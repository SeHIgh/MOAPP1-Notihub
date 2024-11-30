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
import com.example.notihub.algorithm.shouldNotify
import com.example.notihub.database.AppDatabase
import com.example.notihub.database.KNUAnnouncementEntity
import com.example.notihub.database.UserPreferenceDao
import com.example.notihub.database.UserPreferenceEntity
import com.example.notihub.database.toEntity
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
        const val TOP_KEYWORDS = 10

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
            val newAnnouncements = announcements.filter {
                announcement -> announcementDao.getAnnouncementById(announcement.id, announcement.source) == null
            }
            for ((i, announcement) in newAnnouncements.withIndex()) {
                launch {
                    getKNUCSEAnnouncementDetail(announcement)
                    if (announcement.body.isNotEmpty() && (i == 0 || i == 1))
                    // if (announcement.body.isNotEmpty())
                        geminiChannel.send(announcement)
                }
            }
            newAnnouncements
        })
        jobs.add(async {
            val announcements = getKNUITAnnouncementList()
            val newAnnouncements = announcements.filter {
                announcement -> announcementDao.getAnnouncementById(announcement.id, announcement.source) == null
            }
            for (announcement in newAnnouncements) {
                launch {
                    getKNUITAnnouncementDetail(announcement)
                    // if (announcement.body.isNotEmpty())
                    //     geminiChannel.send(announcement)
                }
            }
            newAnnouncements
        })

        for (job in jobs) {
            newItems.addAll(job.await())
        }
        geminiChannel.close()
        geminiJob.join()
        newItems.sort()

        val topKeywords = userPreferenceDao.getTopKeywords(TOP_KEYWORDS)
        newItems.forEach {
            Log.d("DB", "새로운 알림이 저장됨: ${it.title}")
            announcementDao.insertAnnouncement(it.toEntity())
            if (shouldNotify(it.keywords, topKeywords))
                showNewAnnouncementNotification(it)
        }
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
}