package me.allons.filebasedcalendar

import android.accounts.*
import android.os.Bundle
import android.content.Context
import android.content.Intent


/*
 * Implement AbstractAccountAuthenticator and stub out all
 * of its methods
 */
class Authenticator : AbstractAccountAuthenticator {
    private val _context: Context

    constructor(context : Context) : super(context) {
        _context = context
    }

    override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle {
        val clazz = AuthenticatorActivity::class.java
        val intent = Intent(_context, clazz)
        intent.putExtra(AuthenticatorActivity.EXTRA_ACCOUNT_TYPE, accountType)
        intent.putExtra(AuthenticatorActivity.EXTRA_AUTH_TYPE, authTokenType)
        intent.putExtra(AuthenticatorActivity.EXTRA_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
        account ?: throw IllegalArgumentException()

        val clazz = AuthenticatorActivity::class.java
        val intent = Intent(_context, clazz)
        intent.putExtra(AuthenticatorActivity.EXTRA_ACCOUNT_TYPE, account.type)
        intent.putExtra(AuthenticatorActivity.EXTRA_AUTH_TYPE, authTokenType)
        intent.putExtra(AuthenticatorActivity.EXTRA_IS_ADDING_NEW_ACCOUNT, true)
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        val bundle = Bundle()
        bundle.putParcelable(AccountManager.KEY_INTENT, intent)
        return bundle
    }

    override fun getAuthTokenLabel(authTokenType: String?): String {
        throw UnsupportedOperationException()
    }

    override fun updateCredentials(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle {
        throw UnsupportedOperationException()
    }

    override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle {
        throw UnsupportedOperationException()
    }
}