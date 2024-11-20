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
import java.util.Collections

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

    private val binders = mutableListOf<InfoBinder>()

    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch(Dispatchers.Default) {
            val jobs = mutableListOf<Deferred<List<KNUAnnouncement>>>()
            val newItems = mutableListOf<KNUAnnouncement>()

            jobs.add(async {
                val announcements = getKNUCSEAnnouncementList()
                // TODO: 새 글인지 확인
                for (announcement in announcements) {
                    launch {
                        getKNUCSEAnnouncementDetail(announcement)
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
                    }
                }
                announcements
            })

            for (job in jobs) {
                newItems.addAll(job.await())
            }
            newItems.sort()

            // TODO: Gemini

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