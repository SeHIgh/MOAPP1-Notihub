package com.example.notihub.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. App is starting automatically.")


            val alarmServiceIntent = Intent(context, AlarmService::class.java)
            context.startService(alarmServiceIntent) // 서비스 실행
        }
    }
}