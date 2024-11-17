package com.example.notihub.parsers

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InfoPollingService : LifecycleService() {
    class InfoBinder: Binder() {
        private val callbacks = mutableListOf<(KNUAnnouncement) -> Unit>()
        private val allDoneCallbacks = mutableListOf<(Int) -> Unit>()

        fun addItemCallback(callback: (KNUAnnouncement) -> Unit) {
            callbacks.add(callback)
        }
        fun removeItemCallback(callback: (KNUAnnouncement) -> Unit) {
            callbacks.remove(callback)
        }
        suspend fun onItemResult(result: KNUAnnouncement) {
            for (callback in callbacks) {
                withContext(Dispatchers.Main.immediate) { callback(result) }
            }
        }

        fun addAllDoneCallback(callback: (Int) -> Unit) {
            allDoneCallbacks.add(callback)
        }
        fun removeAllDoneCallback(callback: (Int) -> Unit) {
            allDoneCallbacks.remove(callback)
        }
        suspend fun onAllDone(newCount: Int) {
            for (callback in allDoneCallbacks) {
                withContext(Dispatchers.Main.immediate) { callback(newCount) }
            }
        }
    }

    private val binders = mutableListOf<InfoBinder>()

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            val jobs = mutableListOf<Deferred<Int>>()
            var newAnnouncementCount = 0

            jobs.add(async {
                val announcements = getKNUCSEAnnouncementList()
                // TODO: 새 글인지 확인
                for (announcement in announcements) {
                    launch {
                        getKNUCSEAnnouncementDetail(announcement)
                        for (binder in binders) {
                            binder.onItemResult(announcement)
                        }
                    }
                }
                announcements.size
            })
            jobs.add(async {
                val announcements = getKNUITAnnouncementList()
                // TODO: 새 글인지 확인
                for (announcement in announcements) {
                    launch {
                        getKNUITAnnouncementDetail(announcement)
                        for (binder in binders) {
                            binder.onItemResult(announcement)
                        }
                    }
                }
                announcements.size
            })
            for (job in jobs) {
                newAnnouncementCount += job.await()
            }

            // TODO: Gemini

            // TODO: 유사도

            // TODO: 새 글 알림
            for (binder in binders) {
                binder.onAllDone(newAnnouncementCount)
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