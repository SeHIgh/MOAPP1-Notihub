package com.example.notihub.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted. Setting up immediate and repeated AlarmManager.")

            // AlarmManager 설정
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 부팅 직후 실행 (Handler를 사용한 즉시 실행)
            Handler(Looper.getMainLooper()).post {
                context.sendBroadcast(alarmIntent) // 직접 브로드캐스트를 보냄
            }

            // 반복 알람 설정 (10초 간격)
            val triggerTime = System.currentTimeMillis() // 현재 시간
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime + 10 * 1000L, // 첫 알람 이후 10초 후부터 반복 시작
                10 * 1000L, // 10초 간격
                pendingIntent
            )
        }
    }
}
