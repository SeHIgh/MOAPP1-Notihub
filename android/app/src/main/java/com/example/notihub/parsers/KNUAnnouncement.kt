package com.example.notihub.parsers

enum class KNUAnnouncementSource {
    CSE, IT
}

data class KNUAnnouncement(
    val source: KNUAnnouncementSource,
    val id: Int,
    val title: String,
    val time: Time,
    val bodyUrl: String,
    var body: String
) {
    data class Time (
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int
    ) {
        override fun toString(): String {
            return "$year-$month-$day ${"%02d".format(hour)}:${"%02d".format(minute)}"
        }
    }
}
