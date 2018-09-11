package me.allons.filebasedcalendar

import android.accounts.Account
import android.accounts.AccountManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SyncResult
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.webkit.PermissionRequest

import kotlinx.android.synthetic.main.activity_authenticator.*
import kotlinx.android.synthetic.main.content_authenticator.*
import java.util.jar.Manifest

class AuthenticatorActivity : AppCompatActivity {
    constructor() {
    }

    companion object {

        val EXTRA_ACCOUNT_TYPE = AuthenticatorActivity::class.simpleName + ".extra.ACCOUNT_TYPE"
        val EXTRA_AUTH_TYPE = AuthenticatorActivity::class.simpleName + ".extra.AUTH_TYPE"
        val EXTRA_IS_ADDING_NEW_ACCOUNT = AuthenticatorActivity::class.simpleName + ".extra.IS_ADDING_NEW_ACCOUNT"
        const val PICKFILE_REQUEST_CODE = 23989
        const val CALENDAR_REQUEST_CODE = 32098
    }

    private var _accountName : String = ""

    private var accountName : String
        get() = _accountName
        set(v) {
            _accountName = v
            maybeEnableSave()
        }

    private var _selectedPath : Uri = Uri.parse("content://")

    private var selectedPath : Uri
        get() = _selectedPath
        set(v) {
            _selectedPath = v
            account_path.text = Uri.decode(_selectedPath.pathSegments.lastOrNull())
            maybeEnableSave()
        }

    private fun maybeEnableSave() {
        account_confirm.isEnabled = selectedPath.pathSegments.isNotEmpty() && accountName.isNotBlank() && accountName.matches(Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,63}$", RegexOption.IGNORE_CASE))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        data ?: throw IllegalArgumentException()
        requestCode == PICKFILE_REQUEST_CODE || return

        selectedPath = data.data

        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == CALENDAR_REQUEST_CODE) {
            calendarGranted()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun calendarGranted() {
        val userdata = Bundle()
        userdata.putString(App.ACCOUNT_DATA_STORAGE_PATH, selectedPath.toString())

        val accountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

        val account = Account(accountName, resources.getString(R.string.account_type))

        accountManager.addAccountExplicitly(account, null, userdata)
        ContentResolver.setSyncAutomatically(account, CalendarContract.AUTHORITY, true)

        val provider = contentResolver.acquireContentProviderClient(CalendarContract.CONTENT_URI)
        DefaultCalendar.findOrCreate(account, provider)

        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_authenticator)
        setSupportActionBar(toolbar)

        account_path_selector.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            val chooser = Intent.createChooser(intent, "Choose directory containing ICS files")
            startActivityForResult(chooser, PICKFILE_REQUEST_CODE)
        }

        account_cancel.setOnClickListener {
            finish()
        }

        account_name_input.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                accountName = s.toString()
            }
        })

        account_confirm.setOnClickListener {
            if(intent.getBooleanExtra(EXTRA_IS_ADDING_NEW_ACCOUNT, true)) {
                if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_CALENDAR, android.Manifest.permission.READ_CALENDAR), CALENDAR_REQUEST_CODE)
                }
                else {
                    calendarGranted()
                }
            }
        }

        manual_run.setOnClickListener {
            AsyncTask.execute {
                val accountManager = getSystemService(Context.ACCOUNT_SERVICE) as AccountManager

                val accounts = accountManager.getAccountsByType(resources.getString(R.string.account_type))

                for(account in accounts) {
                    val res = SyncResult()
                    val provider = contentResolver.acquireContentProviderClient(CalendarContract.CONTENT_URI)
                    SyncAdapter(baseContext, true, false).onPerformSync(account, Bundle.EMPTY, CalendarContract.AUTHORITY, provider, res)
                }
            }
        }
    }

}

