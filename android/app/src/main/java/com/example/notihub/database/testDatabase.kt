package com.example.notihub.database

import android.content.Context
import android.util.Log
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource
import kotlinx.coroutines.runBlocking


fun testDatabase(context: Context) {
    // Room 데이터베이스 인스턴스 생성
    val db = AppDatabase.getDatabase(context)
    val announcementDao = db.knuAnnouncementDao()
    val userPreferenceDao = db.userPreferenceDao()

    runBlocking {
        // KNUAnnouncement 테스트 데이터 추가 (여러 개)
        val announcements = listOf(
            KNUAnnouncementEntity(
                source = KNUAnnouncementSource.CSE,
                id = 1,
                title = "CSE Announcement 1",
                time = Converters().fromTime(KNUAnnouncement.Time(2024, 1, 1, 12, 0)),
                bodyUrl = "https://example.com/1",
                body = "CSE Announcement Body 1",
                summary = "This is CSE summary 1",
                keywords = mutableListOf("keyword1", "keyword2")
            ),
            KNUAnnouncementEntity(
                source = KNUAnnouncementSource.IT,
                id = 2,
                title = "IT Announcement 2",
                time = Converters().fromTime(KNUAnnouncement.Time(2024, 1, 2, 14, 0)),
                bodyUrl = "https://example.com/2",
                body = "IT Announcement Body 2",
                summary = "This is IT summary 2",
                keywords = mutableListOf("keyword3", "keyword4")
            ),
            KNUAnnouncementEntity(
                source = KNUAnnouncementSource.CSE,
                id = 3,
                title = "CSE Announcement 3",
                time = Converters().fromTime(KNUAnnouncement.Time(2024, 1, 3, 10, 0)),
                bodyUrl = "https://example.com/3",
                body = "CSE Announcement Body 3",
                summary = "This is CSE summary 3",
                keywords = mutableListOf("keyword5", "keyword6")
            )
        )

        announcements.forEach { announcementDao.insertAnnouncement(it) }
        Log.d("DatabaseTest", "Inserted Announcements: ${announcementDao.getAllAnnouncements()}")

        // 모든 공지사항 조회
        val allAnnouncements = announcementDao.getAllAnnouncements()
        Log.d("DatabaseTest", "All Announcements: $allAnnouncements")

        // 공지사항 삭제 테스트
        announcementDao.deleteAnnouncement(1)
        Log.d("DatabaseTest", "Announcement after deletion of ID 1: ${announcementDao.getAnnouncementById(1)}")

        // UserPreference 테스트 데이터 추가 (여러 개)
        val preferences = listOf(
            UserPreferenceEntity(
                keyword = "keyword1",
                weight = 1.0,
                feedback = 1
            ),
            UserPreferenceEntity(
                keyword = "keyword2",
                weight = 2.0,
                feedback = -1
            ),
            UserPreferenceEntity(
                keyword = "keyword3",
                weight = 3.0,
                feedback = 1
            )
        )

        preferences.forEach { userPreferenceDao.insertOrUpdatePreference(it) }
        Log.d("DatabaseTest", "Inserted Preferences: ${userPreferenceDao.getAllPreferences()}")

        // 가중치 업데이트 테스트
        userPreferenceDao.updateWeightByKeyword("keyword1", 5.0)
        Log.d("DatabaseTest", "Updated Preference for 'keyword1': ${userPreferenceDao.getPreferenceByKeyword("keyword1")}")

        // 모든 선호도 조회
        val allPreferences = userPreferenceDao.getAllPreferences()
        Log.d("DatabaseTest", "All Preferences: $allPreferences")

        // 모든 선호도 초기화 테스트
        userPreferenceDao.resetAllPreferences()
        Log.d("DatabaseTest", "Preferences after reset: ${userPreferenceDao.getAllPreferences()}")

        // 선호도 삭제 테스트
        userPreferenceDao.deletePreferenceByKeyword("keyword1")
        Log.d("DatabaseTest", "Preference after deletion of 'keyword1': ${userPreferenceDao.getPreferenceByKeyword("keyword1")}")
    }
}

