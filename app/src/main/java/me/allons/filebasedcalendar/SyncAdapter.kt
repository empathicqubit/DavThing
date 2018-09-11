package me.allons.filebasedcalendar

import android.accounts.Account
import android.accounts.AccountManager
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.util.Log
import net.fortuna.ical4j.util.CompatibilityHints
import net.fortuna.ical4j.util.MapTimeZoneCache
import java.util.*

class SyncAdapter : AbstractThreadedSyncAdapter {

    constructor(context : Context, autoInitialize : Boolean)
            : super(context, autoInitialize) {
    }

    constructor(context : Context, autoInitialize: Boolean, allowParallelSyncs : Boolean)
            : super(context, autoInitialize, allowParallelSyncs) {
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
        val properties = Properties()

        Thread.currentThread().contextClassLoader = context.classLoader

        Log.i(App.LOG_TAG, "A sync was initiated!")

        account ?: throw IllegalArgumentException("account is null")
        provider ?: throw IllegalArgumentException("provider is null")
        extras ?: return

        val mgr = context.getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

        val storagePath = mgr.getUserData(account, App.ACCOUNT_DATA_STORAGE_PATH)

        try {
            CalendarStore(Uri.parse(storagePath), context).syncEvents(provider, account, CalendarStore.SyncDirection.TO_FILESYSTEM)
        }
        catch(e : SecurityException) {
            Log.e(App.LOG_TAG, "There was a problem getting the events", e)
        }
    }
}