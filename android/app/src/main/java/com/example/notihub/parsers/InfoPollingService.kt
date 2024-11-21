package com.example.notihub.parsers

import Notification
import UserPreference
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.notihub.BuildConfig
import com.example.notihub.database.KNUAnnouncementEntity
import com.example.notihub.database.UserPreferenceDao
import com.example.notihub.database.UserPreferenceEntity
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class InfoPollingService : LifecycleService() {
    class InfoBinder : Binder() {
        private val newItemsCallback = mutableListOf<(List<KNUAnnouncement>) -> Unit>()

        fun addNewItemsCallback(callback: (List<KNUAnnouncement>) -> Unit) {
            newItemsCallback.add(callback)
        }

        fun removeNewItemsCallback(callback: (List<KNUAnnouncement>) -> Unit) {
            newItemsCallback.remove(callback)
        }

        suspend fun onNewItems(newItems: List<KNUAnnouncement>) {
            for (callback in newItemsCallback) {
                withContext(Dispatchers.Main.immediate) { callback(newItems) }
            }
        }
    }

    private data class GeminiResponse(
        val summary: String,
        val keywords: List<String>
    )

    companion object {
        private const val PROMPT_HEADER =
            "아래 글을 처리해서 아래의 json 형식으로 알려 줘." +
                    "{\"summary\": \"5문장으로 요약된 글\", \"keywords\": [\"가장중요한단어1\", ..., \"가장중요한단어5\"]}\n\n"
        private val FIVE_SECONDS = 5.seconds
    }

    private val binders = mutableListOf<InfoBinder>()
    private val geminiModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        job = launchJob()
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        val binder = InfoBinder()
        binders.add(binder)
        if (job?.isCompleted == true)
            job = launchJob()
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // TODO: 할당된 Binder remove (어떻게?)
        return super.onUnbind(intent)
    }

    private fun launchJob() = lifecycleScope.launch(Dispatchers.Default) {
        val geminiChannel = Channel<KNUAnnouncement>()
        val newItems = mutableListOf<KNUAnnouncement>()
        val jobs = mutableListOf<Deferred<List<KNUAnnouncement>>>()
        val geminiJob: Job = launch { geminiLoop(geminiChannel) }

        jobs.add(async {
            val announcements = getKNUCSEAnnouncementList()
            // TODO: 새 글인지 확인
            // for (announcement in announcements) {
            for ((i, announcement) in announcements.withIndex()) {
                launch {
                    getKNUCSEAnnouncementDetail(announcement)
                    // if (announcement.body.isNotEmpty())
                    if (announcement.body.isNotEmpty() && (i == 0 || i == 1))
                        geminiChannel.send(announcement)
                }
            }
            announcements
        })
        jobs.add(async {
            val announcements = getKNUITAnnouncementList()
            // TODO: 새 글인지 확인
            for (announcement in announcements) {
                launch {
                    getKNUITAnnouncementDetail(announcement)
                    // if (announcement.body.isNotEmpty())
                    //     geminiChannel.send(announcement)
                }
            }
            announcements
        })

        for (job in jobs) {
            newItems.addAll(job.await())
        }
        geminiChannel.close()
        geminiJob.join()
        newItems.sort()

        // TODO: 유사도
        // DB: 알림 데이터 처리 후 유저 피드백 처리
        for (announcement in newItems) {
            // KNUAnnouncement을 KNUAnnouncementEntity로 변환
            val announcementEntity = convertToKNUAnnouncementEntity(announcement)
            // TODO: 실제 피드백을 받을 방법을 결정 필요
            processNotificationFeedback(userPreferenceDao, announcement, "like")
        }

        // TODO: 새 글 알림
        for (binder in binders) {
            binder.onNewItems(newItems)
        }

        // TODO: AlarmManager

        stopSelf()
    }

    private suspend fun geminiLoop(channel: Channel<KNUAnnouncement>) =
        withContext(Dispatchers.IO) {
            val gson = Gson()
            for (announcement in channel) {
                // announcement.summary = announcement.body
                val elapsedTime = measureTime {
                    Log.d(
                        "Notihub::Gemini",
                        "URL: ${announcement.bodyUrl} | Body: ${announcement.body}"
                    )
                    var response = geminiModel.generateContent(
                        PROMPT_HEADER + announcement.body
                    ).text ?: ""
                    response = response.trim().trim('`')
                    if (response.startsWith("json"))
                        response = response.drop(4)
                    Log.d("Notihub::Gemini", response)

                    val data = gson.fromJson(response, GeminiResponse::class.java)
                    announcement.run {
                        summary = data.summary
                        keywords.addAll(data.keywords)
                    }
                }
                if (elapsedTime < FIVE_SECONDS)
                    delay(FIVE_SECONDS - elapsedTime)
            }
        }

    // DB: KNUAnnouncement을 KNUAnnouncementEntity로 변환하는 함수
    // suspand 필요 유무?
    private fun convertToKNUAnnouncementEntity(announcement: KNUAnnouncement): KNUAnnouncementEntity {
        return KNUAnnouncementEntity(
            source = announcement.source,           // source는 KNUAnnouncement에서 가져온 그대로
            id = announcement.id,
            title = announcement.title,
            time = announcement.time.toString(),    // Time 객체를 String으로 변환
            bodyUrl = announcement.bodyUrl,
            body = announcement.body,
            summary = announcement.summary,
            keywords = announcement.keywords
        )
    }

    // DB: KNUAnnouncementEntity를 UserPreferenceEntity로 변환하는 함수
    // suspand 필요 유무? - getPreferenceById 사용 위해선 suspend 필요
    private suspend fun convertToUserPreferenceEntity(
        userPreferenceDao: UserPreferenceDao,
        announcementEntity: KNUAnnouncementEntity
    ): UserPreferenceEntity {
        // 기존에 동일한 ID를 가진 preference가 있는지 확인
        val existingPreference = userPreferenceDao.getPreferenceById(announcementEntity.id)
        val preference = existingPreference ?: UserPreferenceEntity(
            id = announcementEntity.id,
            source = announcementEntity.source,
            title = announcementEntity.title,
            time = announcementEntity.time,
            body = announcementEntity.body,
            bodyUrl = announcementEntity.bodyUrl,
            summary = announcementEntity.summary,
            keywords = announcementEntity.keywords
        )
        return preference
    }

    // DB: String -> timestamp  변환 함수
    fun convertToTimestamp(timeString: String): Long {
        // 원하는 날짜 형식에 맞게 SimpleDateFormat 설정
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        // timeString을 Date 객체로 변환
        val date: Date = format.parse(timeString)

        // Date 객체에서 타임스탬프(밀리초) 추출
        return date.time
    }

    // DB: 가중치 계산 및 피드백 처리 함수
    private suspend fun processNotificationFeedback(
        userPreferenceDao: UserPreferenceDao,
        userPreferenceEntity: UserPreferenceEntity,
        feedback: String
    ) {
        // 1. userPreference를 이용해 가중치 계산

        // 가중치 계산에 사용할 Notification 생성 (userPreferenceEntity 파싱)
        val notifications = userPreferenceDao.getAllPreferences().map { _ ->
            Notification(
                message = userPreferenceEntity.body,
                keywords = userPreferenceEntity.keywords,
                timestamp = convertToTimestamp(userPreferenceEntity.time), // 전달값 timestamp 형태로 변환
            )
        }

        val userPreference = UserPreference()

        // 각 알림에 대해 키워드 가중치 업데이트
        for (notification in notifications) {
            userPreference.updateKeywordWeights(notification)
        }

        // 피드백 처리
        notifications.forEach { notification ->
            userPreference.onNotificationFeedback(notification, feedback)
        }

        // 2. 업데이트된 데이터를 UserPreferenceEntity에 갱신
        val updatedPreference = userPreferenceEntity.copy(
            weight = notifications.sumByDouble { notification ->
                userPreference.recommendNotifications(listOf(notification))
                    .firstOrNull()?.keywords?.sum() ?: 0.0
            },  // 여러 알림의 가중치 합계
            feedback = userPreference.feedbackCount + notifications.size // 피드백 횟수 증가
        )

        userPreferenceDao.insertOrUpdatePreference(updatedPreference)
    }

    // private suspend fun getNewAnnouncements(
    //     getList: () -> List<KNUAnnouncement>,
    //     getDetail: (KNUAnnouncement) -> Unit,
    //     previousAnnouncements: List<KNUAnnouncement>
    // ) = coroutineScope {
    //     val announcements = getList()
    //     for (announcement in announcements) {
    //         launch {
    //             getDetail(announcement)
    //             for (binder in binders) {
    //                 binder.onResult(announcement)
    //             }
    //         }
    //     }
    // }
}