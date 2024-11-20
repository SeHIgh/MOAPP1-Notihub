package com.example.notihub.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File


const val CSEAnnouncementUrl = "https://cse.knu.ac.kr/bbs/board.php?bo_table=sub5_1&lang=kor"


suspend fun getKNUCSEAnnouncementList(): MutableList<KNUAnnouncement> =
    withContext(Dispatchers.Default) {
        val announcements = mutableListOf<KNUAnnouncement>()
        val doc = getURL(CSEAnnouncementUrl) ?: return@withContext announcements
        val rows = doc.select("tbody > tr")

        var announcement: KNUAnnouncement
        for (rowTag in rows) {
            val idTag = rowTag.selectFirst("td.td_num2") ?: continue
            val linkTag = rowTag.selectFirst("div.bo_tit > a") ?: continue
            val dateTag = rowTag.selectFirst("td.td_datetime") ?: continue

            val dateParts = dateTag.text().split("-")
            val announcementURL = linkTag.attr("href")
            announcement = KNUAnnouncement(
                KNUAnnouncementSource.CSE, idTag.text().toInt(), linkTag.text(),
                KNUAnnouncement.Time(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt(), 0, 0),
                announcementURL, "", "", mutableListOf()
            )
            announcements.add(announcement)
        }

        announcements
    }

suspend fun getKNUCSEAnnouncementDetail(announcement: KNUAnnouncement) =
    withContext(Dispatchers.Default) {
        val pageDoc = getURL(announcement.bodyUrl)
        val bodyText = StringBuilder()
        val dateTimeParts = pageDoc?.selectFirst("strong.if_date")?.text()?.run {
            split(" ").last().split(":")
        } ?: return@withContext

        announcement.time.hour = dateTimeParts[0].toInt()
        announcement.time.minute = dateTimeParts[1].toInt()
        for (node in pageDoc.select("div#bo_v_con > p") ?: return@withContext) {
            val innerText = node.text()
            if (innerText.isNotBlank()) {
                bodyText.append(innerText.trim())
                bodyText.append('\n')
            }
        }
        announcement.body = bodyText.toString()
    }

fun main() = runBlocking {
    val announcements: MutableList<KNUAnnouncement> = getKNUCSEAnnouncementList()
    val jobs = mutableListOf<Job>()
    for (announcement in announcements) {
        jobs.add(launch {
            getKNUCSEAnnouncementDetail(announcement)
        })
    }
    jobs.joinAll()
    File("KNUCSEAnnouncement.txt").writeText(announcements.toString())
}
