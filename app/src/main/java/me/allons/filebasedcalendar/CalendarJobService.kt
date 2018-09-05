package me.allons.filebasedcalendar

import android.accounts.Account
import android.accounts.AccountManager
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteQueryBuilder
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.util.Log

class CalendarJobService : JobService() {
    override fun onStartJob(params: JobParameters?) : Boolean {
        if(params == null) {
            jobFinished(params, true)
            return false
        }

        Log.i(App.LOG_TAG, "I'm gonna call you calendar, cuz your days are numbered!")

        val provider = baseContext.contentResolver.acquireContentProviderClient(CalendarContract.CONTENT_URI)
        val mgr = baseContext.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

        val account = mgr.getAccountsByType(resources.getString(R.string.account_type))[0]

        try {
            ContentResolver.requestSync(account, CalendarContract.AUTHORITY, Bundle.EMPTY)
        }
        catch(e : SecurityException) {
            Log.e(App.LOG_TAG, "There was a problem getting the events", e)
        }


        jobFinished(params, true)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

}
