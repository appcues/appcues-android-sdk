package com.appcues.debugger.model

import java.util.Calendar
import java.util.Date

internal object DebuggerConstants {

    @SuppressWarnings("MagicNumber")
    // used in multiple places for ui testing purposes
    val testDate: Date = Calendar.getInstance().apply {
        set(2024, Calendar.SEPTEMBER, 19, 0, 23, 0)
    }.time
}
