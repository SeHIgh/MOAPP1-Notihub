package com.example.notihub.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
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

@Entity(
    tableName = "user_preference",
    indices = [Index(value = ["keyword"], unique = true)]
)
@TypeConverters(Converters::class) // Custom TypeConverter 사용
data class UserPreferenceEntity(
    @PrimaryKey val keyword: String,  // 가중치 부여할 단어
    var weight: Double = 0.0,  // 초기 가중치 (기본값 0.0)
    var feedback: Int = 0  // 피드백: 좋아요(+1), 싫어요(-1), 무응답(0)
    // val time: String = "2024-01-01 00:00",  // 변환 필요 : Time 객체 -> String

)

// UserPreferenceEntity 변경 사항:
// - PrimaryKey : 단일 keyword로 변경 -> 중복된 keyword에 대한 가중치 저장을 방지
// - indices 추가 -> keyword의 고유성을 보장 & 데이터베이스 접근 시 내부적으로 효율적인 처리가 이루어짐
// - weight 필드 -> Double : 연한 가중치 값을 저장가능
// - feedback 필드를 추가하여 사용자가 좋아요/싫어요를 선택한 내용을 Int로 저장가능
// - feedback : 좋아요(1), 싫어요(-1), 무응답(0)
