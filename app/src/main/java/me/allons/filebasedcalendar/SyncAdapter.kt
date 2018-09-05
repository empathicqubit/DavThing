package me.allons.filebasedcalendar

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import android.os.Environment
import android.provider.CalendarContract
import android.util.Log

class SyncAdapter : AbstractThreadedSyncAdapter {
    constructor(context : Context, autoInitialize : Boolean)
            : super(context, autoInitialize) {
    }

    constructor(context : Context, autoInitialize: Boolean, allowParallelSyncs : Boolean)
            : super(context, autoInitialize, allowParallelSyncs) {
    }

    override fun onPerformSync(account: Account?, extras: Bundle?, authority: String?, provider: ContentProviderClient?, syncResult: SyncResult?) {
        account ?: throw IllegalArgumentException("account is null")
        provider ?: throw IllegalArgumentException("provider is null")

        Log.i(App.LOG_TAG, "A sync was initiated!")

        try {
            CalendarStore(Environment.getExternalStorageDirectory().absolutePath + "/Radicale/collections/collection-root/empathicqubit@allons.me/142248e5-2689-a064-f17c-a56d3509c36f").syncEvents(provider, account, CalendarStore.SyncDirection.TO_FILESYSTEM)
        }
        catch(e : SecurityException) {
            Log.e(App.LOG_TAG, "There was a problem getting the events", e)
        }
    }
}