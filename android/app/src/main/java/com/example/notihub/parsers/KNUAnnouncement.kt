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
    var body: String,
    var summary: String,
    val keywords: MutableList<String>,
): Comparable<KNUAnnouncement> {
    data class Time (
        val year: Int,
        val month: Int,
        val day: Int,
        val hour: Int,
        val minute: Int
    ): Comparable<Time> {
        override fun toString(): String {
            return "$year-$month-$day ${"%02d".format(hour)}:${"%02d".format(minute)}"
        }

        override fun compareTo(other: Time): Int =
            if (year != other.year)
                other.year - year
            else if (month != other.month)
                other.month - month
            else if (day != other.day)
                other.day - day
            else if (hour != other.hour)
                other.hour - hour
            else
                other.minute - minute
    }

    override fun compareTo(other: KNUAnnouncement): Int =
        if (time != other.time)
            time.compareTo(other.time)
        else
            -source.compareTo(other.source)
}
