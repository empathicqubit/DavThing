package me.allons.davthing

import android.accounts.AccountManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Environment
import android.os.FileObserver
import android.os.IBinder
import android.renderscript.RenderScript
import android.support.v4.app.NotificationCompat
import android.util.Log
import java.io.File

class CalendarFileService : Service() {
    companion object {
        private const val NOTIFICATION : Int = R.string.service_started

        val ACTION_SUSPEND = App::class.qualifiedName + ".action.SUSPEND_FILE_SERVICE"
        val ACTION_RESUME = App::class.qualifiedName + ".action.RESUME_FILE_SERVICE"
    }

    inner class CalendarFileServiceBinder : Binder() {
        fun getService() : CalendarFileService {
            return this@CalendarFileService
        }
    }

    private val binder : IBinder = CalendarFileServiceBinder()

    private fun init() {
        val am = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

        val accounts = am.getAccountsByType(App.ACCOUNT_TYPE)

        for(account in accounts) {
            val storagePath = am.getUserData(account, App.ACCOUNT_DATA_STORAGE_PATH)
            val filePath = File(Environment.getExternalStorageDirectory().path, Uri.decode(Uri.parse(storagePath).lastPathSegment).split(":")[1]).canonicalPath

            val observer = CalendarFileObserver(am, filePath, FileObserver.MOVED_TO or FileObserver.MODIFY or FileObserver.CREATE or FileObserver.DELETE)
            observer.startWatching()

            Log.v(App.LOG_TAG, "Listening to $filePath")

            _observers.add(observer)
        }

        Log.i(App.LOG_TAG, "Started file monitoring service")
    }

    private val _initted: Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(_initted && intent != null) {
            when(intent.action) {
                ACTION_RESUME -> resume()
                ACTION_SUSPEND -> supend()
            }
        }
        else {
            init()
        }

        return START_NOT_STICKY
    }

    private fun supend() {
        for(o in _observers) {
            o.stopWatching()
        }
        Log.v(App.LOG_TAG, "File watcher was suspended.")
    }

    private fun resume() {
        for(o in _observers) {
            o.startWatching()
        }
        Log.v(App.LOG_TAG, "File watcher was resumed.")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private var _notificationManager: NotificationManager? = null

    private var _observers: MutableList<CalendarFileObserver> = mutableListOf()

    override fun onCreate() {
        _notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        showNotification()
    }

    override fun onDestroy() {
        for(o in _observers) {
            o.stopWatching()
        }
        Log.v(App.LOG_TAG, "Service destroyed.")
    }

    private fun showNotification() {
        val text = getText(R.string.service_started)

        val contentIntent = PendingIntent.getActivity(this, 0, Intent(this, AuthenticatorActivity::class.java), 0)

        val notification = NotificationCompat.Builder(this, App.NOTIFICATION_CHANNEL_ID)
                .setTicker(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.notification_icon_background)
                .setContentTitle(getText(R.string.service_label))
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(contentIntent)
                .build()

        _notificationManager!!.notify(NOTIFICATION, notification)
    }
}