package com.appcues.debugger.model

import java.util.Calendar
import java.util.Date

internal object DebuggerConstants {

    @SuppressWarnings("MagicNumber")
    // used in multiple places for ui testing purposes
    val testDate: Date = Calendar.getInstance().apply {
        timeInMillis = 1726719780000L
    }.time
}
