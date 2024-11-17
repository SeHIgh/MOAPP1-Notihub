package com.example.notihub.parsers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOError


suspend fun getURL(url: String): Document? = withContext(Dispatchers.IO) {
    try {
        Jsoup.connect(url).userAgent(url).get()
    } catch(e: IOError) {
        null
    }
}
