package me.allons.filebasedcalendar

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentUris
import android.content.ContentValues
import android.provider.CalendarContract
import at.bitfire.ical4android.AndroidCalendar
import at.bitfire.ical4android.AndroidCalendarFactory

class DefaultCalendar (
        account: Account,
        providerClient: ContentProviderClient,
        id: Long
): AndroidCalendar<DefaultEvent>(account, providerClient, DefaultEvent.Factory, id) {

    companion object {
        fun findOrCreate(account: Account, provider: ContentProviderClient): DefaultCalendar {
            val calendars = AndroidCalendar.find(account, provider, Factory, null, null)
            return if (calendars.isEmpty()) {
                val values = ContentValues(3)
                values.put(CalendarContract.Calendars.NAME, account.name)
                values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, account.name)
                values.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                values.put(CalendarContract.Calendars.OWNER_ACCOUNT, account.name)
                values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
                val uri = AndroidCalendar.create(account, provider, values)

                DefaultCalendar(account, provider, ContentUris.parseId(uri))
            } else
                calendars.first()
        }
    }


    object Factory: AndroidCalendarFactory<DefaultCalendar> {
        override fun newInstance(account: Account, provider: ContentProviderClient, id: Long) =
                DefaultCalendar(account, provider, id)
    }

}
