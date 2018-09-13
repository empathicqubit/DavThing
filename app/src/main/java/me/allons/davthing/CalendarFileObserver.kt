package me.allons.davthing

import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.os.Bundle
import android.os.FileObserver
import android.provider.CalendarContract
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log
import java.nio.file.Path

class CalendarFileObserver : FileObserver {
    override fun onEvent(event: Int, path: String?) {
        path ?: return

        Log.v(App.LOG_TAG, "Got file event: $event")
        Log.v(App.LOG_TAG, path)

        CalendarStore.isValidFilename(path) || return

        val accounts = _accountManager.getAccountsByType(App.ACCOUNT_TYPE)

        for (account in accounts) {
            Log.v(App.LOG_TAG, "Synchronizing account " + account.name + " " + account.type)

            val bundle = Bundle()
            bundle.putString(SyncAdapter.SYNC_REQUEST_DIRECTION, CalendarStore.SyncDirection.TO_CALENDAR.toString())

            ContentResolver.requestSync(account, CalendarContract.AUTHORITY, bundle)
        }
    }

    private val _accountManager: AccountManager

    constructor(accountManager : AccountManager, path : String) : super(path) {
        _accountManager = accountManager
    }

    constructor(accountManager : AccountManager, path : String, mask : Int) : super(path, mask) {
        _accountManager = accountManager
    }
}