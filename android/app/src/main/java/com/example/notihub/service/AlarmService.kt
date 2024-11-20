package com.example.notihub.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AlarmService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "Service started after boot.")

        // 원하는 작업을 수행

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
