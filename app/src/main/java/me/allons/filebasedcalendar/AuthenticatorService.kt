package me.allons.filebasedcalendar

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
class AuthenticatorService : Service() {
    private var mAuthenticator : Authenticator = Authenticator(this)

    override fun onCreate() {
    }

    override fun onBind(intent: Intent?): IBinder {
        return mAuthenticator.getIBinder()
    }
}