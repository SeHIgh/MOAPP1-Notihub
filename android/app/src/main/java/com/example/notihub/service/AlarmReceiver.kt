package com.example.notihub.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered. Executing service.")

        // AlarmService 실행
        /*
        val serviceIntent = Intent(context, AlarmService::class.java)
        context.startService(serviceIntent)*/
    }
}
