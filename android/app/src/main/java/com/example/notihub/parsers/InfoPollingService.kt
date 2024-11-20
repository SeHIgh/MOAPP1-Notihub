package com.example.notihub.parsers

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.notihub.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class InfoPollingService : LifecycleService() {
    class InfoBinder: Binder() {
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

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch(Dispatchers.Default) {
            val jobs = mutableListOf<Deferred<List<KNUAnnouncement>>>()
            val geminiChannel = Channel<KNUAnnouncement>()
            val newItems = mutableListOf<KNUAnnouncement>()

            // Gemini Job
            launch(Dispatchers.IO) {
                val gson = Gson()
                for (announcement in geminiChannel) {
                    val elapsedTime = measureTime {
                        Log.d("Notihub::Gemini", "URL: ${announcement.bodyUrl} | Body: ${announcement.body}")

                        var response = geminiModel.generateContent(
                            PROMPT_HEADER + announcement.body).text ?: ""
                        response = response.trim().trim('`')
                        if (response.startsWith("json"))
                            response = response.drop(4)

                        Log.d("Notihub::Gemini", response ?: "null")
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

            jobs.add(async {
                val announcements = getKNUCSEAnnouncementList()
                // TODO: 새 글인지 확인
                // for (announcement in announcements) {
                for (announcement in announcements.take(3)) {
                    launch {
                        getKNUCSEAnnouncementDetail(announcement)
                        if (announcement.body.isNotEmpty())
                            geminiChannel.send(announcement)
                    }
                }
                announcements
            })
            jobs.add(async {
                val announcements = getKNUITAnnouncementList()
                // TODO: 새 글인지 확인
                // for (announcement in announcements) {
                for (announcement in announcements.take(3)) {
                    launch {
                        getKNUITAnnouncementDetail(announcement)
                        if (announcement.body.isNotEmpty())
                            geminiChannel.send(announcement)
                    }
                }
                announcements
            })

            for (job in jobs) {
                newItems.addAll(job.await())
            }
            geminiChannel.close()
            newItems.sort()

            // TODO: 유사도

            // TODO: 새 글 알림
            for (binder in binders) {
                binder.onNewItems(newItems)
            }

            // TODO: AlarmManager
        }
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        val binder = InfoBinder()
        binders.add(binder)
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        // TODO: 할당된 Binder remove (어떻게?)
        return super.onUnbind(intent)
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