package com.example.notihub.parsers

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.notihub.BuildConfig
import com.example.notihub.R
import com.example.notihub.activities.ContentActivity
import com.example.notihub.database.AppDatabase
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
import kotlin.time.Duration.Companion.minutes
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
        const val START_SERVICE = 11
        const val SERVICE_ID = 11
        const val REFRESH_NOTIFICATION_ID = 11
        const val REFRESH_NOTIFICATION_CHANNEL = "polling"
        const val NEW_ITEM_NOTIFICATION_CHANNEL = "new-item"

        var newItemNotificationId = 100
    }

    private val binders = mutableListOf<InfoBinder>()
    private val geminiModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    private var job: Job? = null

    // DB: lazy 를 통해 필요한 시점에만 데이터베이스와 연결되도록 함
    private val announcementDao by lazy { AppDatabase.getDatabase(applicationContext).knuAnnouncementDao() }
    private val userPreferenceDao by lazy { AppDatabase.getDatabase(applicationContext).userPreferenceDao() }

    override fun onCreate() {
        super.onCreate()
        startForeground(SERVICE_ID, showPollingNotification())
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
        val geminiChannel = Channel<KNUAnnouncement>(Channel.UNLIMITED)
        val newItems = mutableListOf<KNUAnnouncement>()
        val jobs = mutableListOf<Deferred<List<KNUAnnouncement>>>()
        val geminiJob: Job = launch { geminiLoop(geminiChannel) }

        jobs.add(async {
            val announcements = getKNUCSEAnnouncementList()
            // TODO: 새 글인지 확인
            // DB: 기존의 공지 데이터베이스를 탐색하여 존재 한다면 리스트에서 제외 (중복 방지)
            val newAnnouncements = announcements.filter { announcement ->
                val existingEntity = announcementDao.getAnnouncementById(announcement.id, announcement.source)
                existingEntity == null // Room DB -> 조회 결과 없는 경우 null 반환
            }

            for ((i, announcement) in newAnnouncements.withIndex()) {
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
            // DB: 기존의 공지 데이터베이스를 탐색하여 존재 한다면 리스트에서 제외 (중복 방지)
            val newAnnouncements = announcements.filter { announcement ->
                val existingEntity = announcementDao.getAnnouncementById(announcement.id, announcement.source)
                existingEntity == null
            }
            // TODO: 위와 다른 이유?
            for (announcement in newAnnouncements) {
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

        // DB: 데이터베이스에 새로운 알림 저장
        // TODO: 위치 수정
        saveAnnouncementsToDB(newItems)

        // TODO: 가중치 계산 반영
        // DB: 알림 처리 및 피드백 수집
        // TODO: 위치 수정
        handleNotifications(newItems)

        newItems.forEach { showNewAnnouncementNotification(it) }
        for (binder in binders) {
            binder.onNewItems(newItems)
        }

        val nextStartTime = SystemClock.elapsedRealtime() + 30.minutes.inWholeMilliseconds
        (getSystemService(ALARM_SERVICE) as AlarmManager).setWindow(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            nextStartTime, nextStartTime + 15.minutes.inWholeMilliseconds,
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                PendingIntent.getForegroundService(
                    applicationContext, START_SERVICE,
                    Intent(applicationContext, this@InfoPollingService::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            } else {
                PendingIntent.getService(
                    applicationContext, START_SERVICE,
                    Intent(applicationContext, this@InfoPollingService::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun geminiLoop(channel: Channel<KNUAnnouncement>) =
        withContext(Dispatchers.IO) {
            val gson = Gson()
            for (announcement in channel) {
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

    private fun showPollingNotification(): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    REFRESH_NOTIFICATION_CHANNEL,
                    getString(R.string.polling_channel),
                    NotificationManager.IMPORTANCE_MIN
                )
                channel.setShowBadge(false)
                manager.createNotificationChannel(channel)
                NotificationCompat.Builder(this, REFRESH_NOTIFICATION_CHANNEL)
            } else {
                NotificationCompat.Builder(this)
            }

        builder.setSmallIcon(applicationInfo.icon)
        builder.setWhen(System.currentTimeMillis())
        builder.setPriority(NotificationCompat.PRIORITY_LOW)
        builder.setContentTitle(getString(R.string.polling_notification_title))
        builder.setContentText(getString(R.string.polling_notification_description))

        val intent = Intent(applicationContext, this::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 10, intent, PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        val notification = builder.build()
        manager.notify(REFRESH_NOTIFICATION_ID, notification)
        return notification
    }

    private fun showNewAnnouncementNotification(newItem: KNUAnnouncement) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val builder: NotificationCompat.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NEW_ITEM_NOTIFICATION_CHANNEL,
                    getString(R.string.polling_channel),
                    NotificationManager.IMPORTANCE_HIGH
                )
                channel.setShowBadge(false)
                manager.createNotificationChannel(channel)
                NotificationCompat.Builder(this, NEW_ITEM_NOTIFICATION_CHANNEL)
            } else {
                NotificationCompat.Builder(this)
            }

        builder.setSmallIcon(applicationInfo.icon)
        builder.setWhen(System.currentTimeMillis())
        builder.setContentTitle(newItem.title)
        builder.setContentText(newItem.summary)

        val pendingIntent = PendingIntent.getActivity(
            this, newItemNotificationId,
            Intent(applicationContext, ContentActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(ContentActivity.DATA, newItem),
            PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        manager.notify(newItemNotificationId++, builder.build())
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

    // DB: KNUAnnouncement을 KNUAnnouncementEntity로 변환하는 함수
    // suspand 필요 유무?
    private fun convertToKNUAnnouncementEntity(announcement: KNUAnnouncement): KNUAnnouncementEntity {
        return KNUAnnouncementEntity(
            source = announcement.source,
            id = announcement.id,
            title = announcement.title,
            time = announcement.time,
            bodyUrl = announcement.bodyUrl,
            body = announcement.body,
            summary = announcement.summary,
            keywords = announcement.keywords
        )
    }

    // DB: 데이터베이스에 새로운 공지사항 저장
    private suspend fun saveAnnouncementsToDB(newItems: List<KNUAnnouncement>) {
        for (announcement in newItems) {
            val announcementEntity = convertToKNUAnnouncementEntity(announcement)
            announcementDao.insertAnnouncement(announcementEntity)
            Log.d("DB", "새로운 알림이 저장됨: ${announcementEntity.title}")
        }
    }

    // DB: 알림 처리 및 사용자 피드백 수집
    private suspend fun handleNotifications(newItems: List<KNUAnnouncement>) {
        for (announcement in newItems) {

            // 사용자 선호도와 관련된 UserPreferenceEntity 생성
            val userPreferences = userPreferenceDao.getAllPreferences()
            for (keyword in announcement.keywords) {
                val existingPreference = userPreferences.find { it.keyword == keyword }
                // 기존에 없는 키워드라면 가중치 0 초기화
                val userPreferenceEntity = existingPreference ?: UserPreferenceEntity(keyword = keyword, weight = 0.0)

                // TODO: 사용자 피드백 받는 방식 논의
                val userFeedback = "" // 실제 피드백 입력 방법에 따라 변경 (예: "like")
                processNotificationFeedback(userPreferenceDao, userPreferenceEntity, userFeedback)
            }
        }
    }

    // DB: 가중치 계산 및 피드백 처리 함수
    private suspend fun processNotificationFeedback(
        userPreferenceDao: UserPreferenceDao,
        userPreferenceEntity: UserPreferenceEntity,
        feedback: String
    ) {
        // 1. 사용자 선호도 데이터베이스에서 기존 키워드를 조회
        val existingPreference = userPreferenceDao.getPreferenceByKeyword(userPreferenceEntity.keyword)

        // 2. 알고리즘을 통해 가중치 계산 및 피드백 처리 (calculateWeight 는 추후 알고리즘 함수로 대체)
        // 알고리즘 리턴값은 최종 계산한 가중치로
        // val finalWeight = calculateWeight(userPreferenceEntity.keyword, existingPreference?.weight ?: 0.0, feedback)
        val finalWeight = 0.0

        // 3. 업데이트된 데이터를 UserPreferenceEntity에 갱신
        val updatedPreference = userPreferenceEntity.copy(
            weight = finalWeight // 알고리즘에서 계산된 새로운 가중치 값 설정
        )

        // 4. 업데이트된 선호도 데이터베이스에 저장
        userPreferenceDao.insertOrUpdatePreference(updatedPreference)
    }
}