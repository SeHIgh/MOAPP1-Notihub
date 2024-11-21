package com.example.notihub.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource

@Entity(tableName = "knu_announcement")
@TypeConverters(Converters::class) // Custom TypeConverter 사용
data class KNUAnnouncementEntity(
    val source: KNUAnnouncementSource, // enum
    @PrimaryKey val id: Int,
    val title: String,
    val time: String = "2024-01-01 00:00",  // 변환 필요 : Time 객체 -> String
    val bodyUrl: String,
    val body: String = "",
    val summary: String = "",
    val keywords: List<String> = emptyList()
)

@Entity(tableName = "user_preference")
data class UserPreferenceEntity(
    @PrimaryKey val id: Int, //  = KNUAnnouncementEntity 의 id
    val source: KNUAnnouncementSource,
    val title: String,
    val time: String,
    val bodyUrl: String,        // 필요 여부 논의
    val body: String = "",
    val summary: String = "",   // 필요 여부 논의
    val keywords: List<String>,
    val weight: Double = 0.0, // 초기 가중치
    val feedback: String = "" // 피드백 ("like"/"dislike"/"")
)
