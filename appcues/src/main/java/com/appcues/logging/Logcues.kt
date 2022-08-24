package com.appcues.logging

import android.util.Log
import com.appcues.LoggingLevel
import com.appcues.LoggingLevel.NONE

internal class Logcues(private val loggingLevel: LoggingLevel) {

    companion object {

        private const val TAG = "Appcues"
    }

    fun info(message: String) {
        if (loggingLevel > NONE) {
            Log.i(TAG, message)
        }
    }

    fun error(message: String) {
        if (loggingLevel > NONE) {
            Log.e(TAG, message)
        }
    }

    fun error(throwable: Throwable) {
        if (loggingLevel > NONE) {
            Log.e(TAG, throwable.message.toString())
        }
    }
}
