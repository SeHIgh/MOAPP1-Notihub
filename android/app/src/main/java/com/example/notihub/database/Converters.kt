package com.example.notihub.database

import androidx.room.TypeConverter
import com.example.notihub.parsers.KNUAnnouncement
import com.example.notihub.parsers.KNUAnnouncementSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromKNUAnnouncementSource(source: KNUAnnouncementSource): String = source.name

    @TypeConverter
    fun toKNUAnnouncementSource(name: String): KNUAnnouncementSource = KNUAnnouncementSource.valueOf(name)

    @TypeConverter
    fun fromKeywords(keywords: MutableList<String>): String = Gson().toJson(keywords)

    @TypeConverter
    fun toKeywords(json: String): MutableList<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Time 객체를 String으로 변환
    @TypeConverter
    fun fromTime(time: KNUAnnouncement.Time): String {
        return time.toString()  // 이미 toString() 메서드가 있으므로 이를 사용
    }

    // String을 Time 객체로 변환
    @TypeConverter
    fun toTime(timeString: String): KNUAnnouncement.Time {
        // "yyyy-MM-dd HH:mm" 형식으로 파싱하여 Time 객체로 변환
        val parts = timeString.split(" ")
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")

        return KNUAnnouncement.Time(
            year = dateParts[0].toInt(),
            month = dateParts[1].toInt(),
            day = dateParts[2].toInt(),
            hour = timeParts[0].toInt(),
            minute = timeParts[1].toInt()
        )
    }
}