package com.appcues.logging

import android.util.Log
import com.appcues.LoggingLevel
import com.appcues.LoggingLevel.DEBUG
import com.appcues.LoggingLevel.NONE

class Logcues(private val loggingLevel: LoggingLevel) {

    companion object {

        private const val TAG = "Appcues"
    }

    fun info(message: String) {
        if (loggingLevel > NONE) {
            Log.i(TAG, message)
        }
    }

    fun error(exception: Exception) {
        if (loggingLevel == DEBUG) {
            throw exception
        } else if (loggingLevel > NONE) {
            Log.e(TAG, exception.message.toString())
        }
    }
}
