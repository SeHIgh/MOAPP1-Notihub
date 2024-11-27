package com.example.notihub.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.notihub.parsers.InfoPollingService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED)
            return
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            context.startForegroundService(Intent(context, InfoPollingService::class.java))
        else
            context.startService(Intent(context, InfoPollingService::class.java))
    }
}
