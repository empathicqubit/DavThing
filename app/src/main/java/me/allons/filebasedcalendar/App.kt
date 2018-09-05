package me.allons.filebasedcalendar

import android.accounts.Account
import android.accounts.AccountManager
import android.app.AlarmManager
import android.app.Application
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.provider.CalendarContract
import android.util.Log
import java.util.*


class App : Application() {
    companion object {
        const val CHANNEL_ID = "me.allons.filebasedcalendar"
        const val CONTENT_JOB_ID = 9248
        const val LOG_TAG = "me.allons.filebasedcalendar"
    }

    override fun onCreate() {
        super.onCreate()

        Log.i(App.LOG_TAG, "Application started")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)

        }

        addAccount()

        scheduleJob()
    }

    fun addAccount() {
        val mgr = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager
        val accountType = resources.getString(R.string.account_type)
        val accountName = resources.getString(R.string.account_name)
        if(mgr.getAccountsByType(accountType).size > 0) {
            return
        }

        val account = Account(accountName, accountType)
        if(!mgr.addAccountExplicitly(account, null, null)) {
            throw Error("Couldn't add account")
        }
    }

    fun scheduleJob() {
        val componentName = ComponentName(applicationContext, CalendarJobService::class.java)

        val jobInfo = JobInfo.Builder(App.CONTENT_JOB_ID, componentName)
                .addTriggerContentUri(JobInfo.TriggerContentUri(CalendarContract.Events.CONTENT_URI, JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS))
                .build()

        val scheduler = baseContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val result = scheduler.schedule(jobInfo)

        if(result == JobScheduler.RESULT_SUCCESS) {
            Log.i(App.LOG_TAG, "JobScheduler success")
        }
        else {
            Log.e(App.LOG_TAG,"JobScheduler failed")
        }
    }
}