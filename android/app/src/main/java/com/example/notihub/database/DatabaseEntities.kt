package com.example.notihub.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource
import com.example.notihub.parsers.Preference

@Entity(tableName = "knu_announcement", primaryKeys = ["source", "id"])
@TypeConverters(Converters::class) // Custom TypeConverter 사용
data class KNUAnnouncementEntity(
    val source: KNUAnnouncementSource, // enum
    val id: Int,
    val title: String,
    val time: KNUAnnouncement.Time,
    val bodyUrl: String,
    val body: String = "",
    val summary: String = "",
    val keywords: List<String> = emptyList(),
    val preference: Preference
)

@Entity(
    tableName = "user_preference",
    indices = [Index(value = ["keyword"], unique = true)]
)
data class UserPreferenceEntity(
    @PrimaryKey val keyword: String,  // 가중치 부여할 단어
    var weight: Double = 0.0  // 초기 가중치 (기본값 0.0)
)

fun KNUAnnouncement.toEntity() = KNUAnnouncementEntity(
    source = this.source,
    id = this.id,
    title = this.title,
    time = this.time,
    bodyUrl = this.bodyUrl,
    body = this.body,
    summary = this.summary,
    keywords = this.keywords,
    preference = this.preference
)

fun KNUAnnouncement.CREATOR.fromEntity(announcement: KNUAnnouncementEntity) = KNUAnnouncement(
    source = announcement.source,
    id = announcement.id,
    title = announcement.title,
    time = announcement.time,
    bodyUrl = announcement.bodyUrl,
    body = announcement.body,
    summary = announcement.summary,
    keywords = announcement.keywords.toMutableList(),
    preference = announcement.preference
)
