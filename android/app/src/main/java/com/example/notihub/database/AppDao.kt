package com.example.notihub.database

import androidx.room.*

@Dao
interface KNUAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: KNUAnnouncementEntity)

    // id로 공지사항 조회
    @Query("SELECT * FROM knu_announcement WHERE id = :id")
    suspend fun getAnnouncementById(id: Int): KNUAnnouncementEntity?

    // id로 공지사항 삭제
    @Query("DELETE FROM knu_announcement WHERE id = :id")
    suspend fun deleteAnnouncement(id: Int)

    // 시간 순(최신 순)으로 모든 공지사항 조회
    @Query("SELECT * FROM knu_announcement ORDER BY time DESC")
    suspend fun getAllAnnouncements(): List<KNUAnnouncementEntity>
}

@Dao
interface UserPreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreference(preference: UserPreferenceEntity)

    // id로 선호도 조회
    @Query("SELECT * FROM user_preference WHERE id = :id")
    suspend fun getPreferenceById(id: Int): UserPreferenceEntity?

    // id로 선호도 삭제
    @Query("DELETE FROM user_preference WHERE id = :id")
    suspend fun deletePreferenceById(id: Int)

    // 가중치 높은 순으로 모든 선호도 조회
    @Query("SELECT * FROM user_preference ORDER BY weight DESC")
    suspend fun getAllPreferences(): List<UserPreferenceEntity>
}