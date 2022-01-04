package com.appcues.logging

import android.util.Log
import com.appcues.Appcues

internal object Logcues {

    private const val TAG = "Appcues"

    private lateinit var loggingLevel: Appcues.LoggingLevel

    fun setLoggingLevel(loggingLevel: Appcues.LoggingLevel) {
        this.loggingLevel = loggingLevel
    }

    fun i(message: String) {
        if (loggingLevel > Appcues.LoggingLevel.NONE) {
            Log.i(TAG, message)
        }
    }
}