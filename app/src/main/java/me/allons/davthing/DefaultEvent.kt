package me.allons.davthing

import android.content.ContentValues
import at.bitfire.ical4android.AndroidCalendar
import at.bitfire.ical4android.AndroidEvent
import at.bitfire.ical4android.AndroidEventFactory
import at.bitfire.ical4android.Event

class DefaultEvent: AndroidEvent {

    constructor(calendar: AndroidCalendar<AndroidEvent>, values: ContentValues)
            : super(calendar, values)

    constructor(calendar: DefaultCalendar, event: Event)
            : super(calendar, event)


    object Factory: AndroidEventFactory<DefaultEvent> {
        override fun fromProvider(calendar: AndroidCalendar<AndroidEvent>, values: ContentValues) =
                DefaultEvent(calendar, values)
    }

}
