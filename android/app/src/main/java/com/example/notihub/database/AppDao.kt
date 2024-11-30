package com.example.notihub.database

import androidx.room.*
import com.example.notihub.parsers.KNUAnnouncementSource

@Dao
interface KNUAnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAnnouncement(announcement: KNUAnnouncementEntity)

    // id로 공지사항 조회
    @Query("SELECT * FROM knu_announcement WHERE id = :id AND source = :source")
    suspend fun getAnnouncementById(id: Int, source: KNUAnnouncementSource): KNUAnnouncementEntity?

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

    // keyword로 선호도 조회
    @Query("SELECT * FROM user_preference WHERE keyword = :keyword")
    suspend fun getPreferenceByKeyword(keyword: String): UserPreferenceEntity?

    // keyword로 선호도 삭제
    @Query("DELETE FROM user_preference WHERE keyword = :keyword")
    suspend fun deletePreferenceByKeyword(keyword: String)

    // 가중치 높은 순으로 모든 선호도 조회
    @Query("SELECT * FROM user_preference ORDER BY weight DESC")
    suspend fun getAllPreferences(): List<UserPreferenceEntity>

    // 가중치 순으로 N개의 키워드 조회
    @Query("SELECT keyword FROM user_preference ORDER BY weight DESC LIMIT :limit")
    suspend fun getTopKeywords(limit: Int): List<String>

    // 모든 선호도를 초기화 (가중치를 0으로 설정)
    @Query("UPDATE user_preference SET weight = 0.0")
    suspend fun resetAllPreferences()

    // 특정 키워드의 가중치 업데이트
    @Query("UPDATE user_preference SET weight = :newWeight WHERE keyword = :keyword")
    suspend fun updateWeightByKeyword(keyword: String, newWeight: Double)
}