package me.allons.filebasedcalendar

import android.accounts.Account
import android.content.ContentProviderClient
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.CalendarContract
import android.support.v4.provider.DocumentFile
import android.util.Log
import at.bitfire.ical4android.*
import net.fortuna.ical4j.util.RandomUidGenerator
import org.apache.commons.lang3.exception.ContextedException
import java.io.*
import java.net.URLConnection
import java.util.*

class CalendarStore {
    private val _doc : DocumentFile
    private val _context : Context
    private val events: MutableMap<String, Event> = mutableMapOf()

    companion object {
        public fun isValidFilename(fileName : String) : Boolean {
            return !fileName.startsWith(".") && fileName.endsWith(".ics") && !fileName.contains(".sync-conflict-")
        }
    }

    public enum class SyncDirection(val value : Int) {
        TO_FILESYSTEM(1),
        TO_CALENDAR(2),
    }

    public constructor(uri : Uri, context: Context) {
        _context = context
        _doc = DocumentFile.fromTreeUri(context, uri) ?: throw IllegalArgumentException()
    }

    public fun refresh() {
        events.clear()

        val files = _doc.listFiles()
        for(file in files) {
            val fileName = file.name ?: continue

            if(!file.isFile || !isValidFilename(fileName)) {
                continue
            }

            val stream = _context.contentResolver.openInputStream(file.uri)
            val reader = InputStreamReader(stream)

            processVEvent(fileName, "", reader)

            reader.close()
            stream.close()
        }
    }

    public fun syncEvents(provider : ContentProviderClient, account : Account, direction : SyncDirection) {
        refresh()

        Log.v(App.LOG_TAG, events.size.toString() + " total files")

        when(direction) {
            SyncDirection.TO_CALENDAR -> {
                val cal = DefaultCalendar.findOrCreate(account, provider)
                val local = cal.queryEvents()
                val batch = BatchOperation(cal.provider)

                for(android in local) {
                    val aEvent = android.event ?: continue

                    if(!events.any { e -> e.value.uid == aEvent.uid }) {
                        Log.v(App.LOG_TAG, "Deleting: " + aEvent.uid)
                        Log.v(App.LOG_TAG, "Title: " + aEvent.summary)
                        android.delete()
                    }
                }

                for(event in events) {
                    var found = false
                    for(android in local) {
                        val aEvent = android.event ?: continue
                        if(aEvent.uid == event.value.uid) {
                            Log.v(App.LOG_TAG,"Updating: " + aEvent.uid)
                            found = true
                            android.update(event.value)
                            break
                        }
                    }

                    if(found) {
                        continue
                    }

                    Log.v(App.LOG_TAG, "Adding: " + event.value.uid)
                    val add = DefaultEvent(cal, event.value)
                    add.add(batch)
                }

                batch.commit()
            }
            SyncDirection.TO_FILESYSTEM -> {
                val cal = DefaultCalendar.findOrCreate(account, provider)
                val full = cal.queryEvents()
                val local = full.map { it.event }

                Log.v(App.LOG_TAG, local.size.toString() + " total calendar items")

                for(android in local) {
                    if(android == null) {
                        Log.v(App.LOG_TAG,"Android event was null")
                        continue
                    }

                    Log.v(App.LOG_TAG, "Android UID: " + android.uid)

                    if(android.uid != null) {
                        for (event in events) {
                            if (android.uid == event.value.uid) {
                                events.remove(event.key)
                            }
                        }
                    }
                    else {
                        android.uid = UUID.randomUUID().toString()
                        Log.v(App.LOG_TAG, "Created ID " + android.uid + " for event.")
                    }

                    events[android.uid + ".ics"] = android
                }

                for(event in events.map { it }.asSequence()) {
                    Log.v(App.LOG_TAG, "Checking event " + event.key)
                    if(event.value.uid + ".ics" != event.key || !local.any { it != null && it.uid == event.value.uid }) {
                        Log.v(App.LOG_TAG, "Removing event " + event.key)

                        val file = _doc.findFile(event.key) ?: continue
                        file.delete()
                        events.remove(event.key)
                    }
                }

                Log.v(App.LOG_TAG, events.size.toString() + " total items to be synced)")

                for(event in events) {
                    val displayName = event.key
                    val file = _doc.findFile(displayName) ?: _doc.createFile("text/calendar", displayName) ?: throw NullPointerException("Could not find or create file")

                    Log.v(App.LOG_TAG, file.uri.lastPathSegment)

                    val stream = _context.contentResolver.openOutputStream(file.uri)

                    Log.v(App.LOG_TAG, "Writing event " + event.key)

                    event.value.write(stream)
                    stream.close()
                }

                for(android in full) {
                    val evt = android.event ?: throw NullPointerException("Missing event")
                    Log.v(App.LOG_TAG, "Updating event " + evt.uid)

                    android.update(evt)
                }
            }
        }
    }

    private fun processVEvent(fileName: String, eTag: String, reader: Reader) {
        var evt : List<Event>
        try {
            Log.v(App.LOG_TAG, "Loading $fileName")
            evt = Event.fromReader(reader)
        } catch (e: InvalidCalendarException) {
            Log.wtf(App.LOG_TAG, "Received invalid iCalendar, ignoring", e)
            return
        }

        if (evt.size == 1) {
            val newData = evt.first()

            events[fileName] = newData
        } else {
            Log.i(App.LOG_TAG, "Received VCALENDAR with not exactly one VEVENT with UID and without RECURRENCE-ID; ignoring $fileName")
        }
    }
}