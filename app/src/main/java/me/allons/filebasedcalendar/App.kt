package me.allons.filebasedcalendar

import android.accounts.AccountManager
import android.app.Application
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.FileObserver
import android.util.Log


class App : Application() {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "me.allons.filebasedcalendar"
        const val LOG_TAG = "me.allons.filebasedcalendar"
        val ACCOUNT_DATA_STORAGE_PATH = App::class.qualifiedName + ".account_data.STORAGE_PATH"
        val SYNC_REQUEST_DIRECTION = App::class.qualifiedName + ".sync_request.DIRECTION"
        const val ACCOUNT_TYPE = "me.allons.filebasedcalendar.mainaccount"

        private val observers : MutableList<FileObserver> = mutableListOf()
    }


    override fun onCreate() {
        super.onCreate()

        Log.v(App.LOG_TAG, "Application started")

        applicationContext.startService(Intent(applicationContext, CalendarFileService::class.java))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

        }
    }
}