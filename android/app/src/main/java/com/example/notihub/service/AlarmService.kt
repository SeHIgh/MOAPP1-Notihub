package com.example.notihub.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log

class AlarmService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            // 10초마다 실행될 작업
            Log.d("MyService", "Service running...")
            handler.postDelayed(this, 10000)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(runnable) // 주기적인 작업 시작
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // 작업 중지
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
