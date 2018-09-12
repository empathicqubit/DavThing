package me.allons.filebasedcalendar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        context.startService(Intent(context, CalendarFileService::class.java))
    }
}