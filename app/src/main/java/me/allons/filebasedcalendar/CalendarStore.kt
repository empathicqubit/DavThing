package me.allons.filebasedcalendar

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.CalendarContract
import android.util.Log
import at.bitfire.ical4android.*
import org.apache.commons.lang3.exception.ContextedException
import java.io.*

class CalendarStore {
    private var _path : String = ""

    public enum class SyncDirection(val value : Int) {
        TO_FILESYSTEM(1),
        TO_CALENDAR(2),
    }

    public constructor(path : String) {
        _path = path
    }

    public fun refresh() {
        events.clear()

        val files = File(_path).listFiles { f -> f.endsWith("*.ics") }
        for(file in files) {
            val reader = FileReader(file)
            processVEvent(file.name, "", reader)
            reader.close()
        }
    }

    public fun syncEvents(provider : ContentProviderClient, account : Account, direction : SyncDirection) {
        refresh()

        when(direction) {
            SyncDirection.TO_CALENDAR -> {
                val cal = DefaultCalendar.findOrCreate(account, provider)
                val local = cal.queryEvents()
                val batch = BatchOperation(cal.provider)

                for(event in events) {
                    var found = false
                    for(android in local) {
                        val aEvent = android.event ?: continue
                        if(aEvent.uid == event.uid) {
                            found = true
                            android.update(event)
                            break
                        }
                    }

                    if(found) {
                        continue
                    }

                    val add = DefaultEvent(cal, event)
                    add.add(batch)
                }

                for(android in local) {
                    val aEvent = android.event ?: continue

                    if(!events.any { e -> e.uid == aEvent.uid }) {
                        android.delete()
                    }
                }
            }
            SyncDirection.TO_FILESYSTEM -> {
                val cal = DefaultCalendar.findOrCreate(account, provider)
                val local = cal.queryEvents().map { a -> a.event }
                for(android in local) {
                    android ?: continue

                    for(event in events) {
                        if(android.uid == event.uid) {
                            events.remove(event)
                            break
                        }
                    }

                    events.add(android)
                }

                for(event in events) {
                    if(!local.contains(event)) {
                        val file = File(_path + "/" + event.uid + ".ics")
                        file.delete()
                    }
                }

                for(event in events) {
                    val stream = FileOutputStream(_path + "/" + event.uid + ".ics")
                    event.write(stream)
                    stream.close()
                }
            }
            else -> throw Exception("What happen?")
        }
    }

    public val events: MutableList<Event> = mutableListOf()

    private fun processVEvent(fileName: String, eTag: String, reader: Reader) {
        var evt : List<Event>
        try {
            evt = Event.fromReader(reader)
        } catch (e: InvalidCalendarException) {
            Log.wtf(App.LOG_TAG, "Received invalid iCalendar, ignoring", e)
            return
        }

        if (evt.size == 1) {
            val newData = events.first()

            events.add(newData)
        } else {
            Log.i(App.LOG_TAG, "Received VCALENDAR with not exactly one VEVENT with UID and without RECURRENCE-ID; ignoring $fileName")
        }
    }
}