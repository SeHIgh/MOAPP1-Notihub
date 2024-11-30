package com.example.notihub.parsers
import kotlinx.parcelize.Parcelize


import android.os.Build
import android.os.Parcel
import android.os.Parcelable

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
): Comparable<KNUAnnouncement>, Parcelable {
    data class Time (
        var year: Int,
        var month: Int,
        var day: Int,
        var hour: Int,
        var minute: Int
    ): Comparable<Time>, Parcelable {
        override fun toString(): String {
            // return "$year-${"%02d".format(month)}-${"%02d".format(day)} ${"%02d".format(hour)}:${"%02d".format(minute)}"
            return "${"%02d".format(month)}-${"%02d".format(day)} ${"%02d".format(hour)}:${"%02d".format(minute)}"
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

        companion object CREATOR: Parcelable.Creator<Time?> {
            override fun createFromParcel(source: Parcel?): Time? {
                source ?: return null
                return Time(
                    source.readInt(), source.readInt(), source.readInt(),
                    source.readInt(), source.readInt()
                )
            }

            override fun newArray(size: Int): Array<Time?> {
                return arrayOfNulls(size)
            }
        }

        override fun describeContents(): Int = 0

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(year)
            dest.writeInt(month)
            dest.writeInt(day)
            dest.writeInt(hour)
            dest.writeInt(minute)
        }
    }

    override fun compareTo(other: KNUAnnouncement): Int =
        if (time != other.time)
            time.compareTo(other.time)
        else
            -source.compareTo(other.source)

    companion object CREATOR: Parcelable.Creator<KNUAnnouncement?> {
        override fun createFromParcel(source: Parcel?): KNUAnnouncement? {
            source ?: return null
            return KNUAnnouncement(
                KNUAnnouncementSource.entries[source.readInt()],
                source.readInt(),
                source.readString() ?: "",
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU) {
                    source.readParcelable(
                        Time::class.java.classLoader,
                        Time::class.java
                    )
                } else {
                    source.readParcelable(Time::class.java.classLoader)
                } ?: Time(0, 0, 0, -1, 0),
                source.readString() ?: "",
                source.readString() ?: "",
                source.readString() ?: "",
                run { val list = mutableListOf<String>(); source.readStringList(list); list }
            )
        }

        override fun newArray(size: Int): Array<KNUAnnouncement?> {
            return arrayOfNulls(size)
        }
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(source.ordinal)
        dest.writeInt(id)
        dest.writeString(title)
        dest.writeParcelable(time, 0)
        dest.writeString(bodyUrl)
        dest.writeString(body)
        dest.writeString(summary)
        dest.writeStringList(keywords)
    }
}
