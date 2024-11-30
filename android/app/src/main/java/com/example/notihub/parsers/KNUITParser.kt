package com.example.notihub.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File


const val ITUrlBase = "https://home.knu.ac.kr"
const val ITAnnouncementUrl = "$ITUrlBase/HOME/it/sub.htm?nav_code=it1623310437&startPage=0"


suspend fun getKNUITAnnouncementList(): MutableList<KNUAnnouncement> =
    withContext(Dispatchers.Default) {
        val announcements = mutableListOf<KNUAnnouncement>()
        val doc = getURL(ITAnnouncementUrl) ?: return@withContext announcements
        val rows = doc.select("tbody > tr")

        var announcement: KNUAnnouncement
        for (rowTag in rows) {
            val idTag = rowTag.selectFirst("td.num") ?: continue
            val linkTag = rowTag.selectFirst("td.subject > a") ?: continue
            val dateTag = rowTag.selectFirst("td.date") ?: continue

            val dateParts = dateTag.text().split("-")
            val announcementURL = linkTag.attr("href")
            announcement = KNUAnnouncement(
                KNUAnnouncementSource.IT, idTag.text().toInt(), linkTag.text(),
                KNUAnnouncement.Time(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt(), 0, 0),
                "$ITUrlBase/$announcementURL"
            )
            announcements.add(announcement)
        }

        announcements
    }

suspend fun getKNUITAnnouncementDetail(announcement: KNUAnnouncement) =
    withContext(Dispatchers.Default) {
        val pageDoc = getURL(announcement.bodyUrl)
        val bodyText = StringBuilder()
        for (node in pageDoc?.select("div.cont > :is(p, div)") ?: return@withContext) {
            val innerText = node.text()
            if (innerText.isNotBlank()) {
                bodyText.append(innerText.trim())
                bodyText.append('\n')
            }
        }
        announcement.body = bodyText.toString()
    }

fun main() = runBlocking {
    val announcements: MutableList<KNUAnnouncement> = getKNUITAnnouncementList()
    val jobs = mutableListOf<Job>()
    for (announcement in announcements) {
        jobs.add(launch {
            getKNUITAnnouncementDetail(announcement)
        })
    }
    jobs.joinAll()
    File("KNUITAnnouncement.txt").writeText(announcements.toString())
}
