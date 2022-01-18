package com.appcues.logging

import android.util.Log
import com.appcues.Appcues
import java.lang.Exception

class Logcues(private val loggingLevel: Appcues.LoggingLevel) {

    companion object {
        private const val TAG = "Appcues"
    }

    fun i(message: String) {
        if (loggingLevel > Appcues.LoggingLevel.NONE) {
            Log.i(TAG, message)
        }
    }

    fun e(exception: Exception) {
        if (loggingLevel > Appcues.LoggingLevel.NONE) {
            Log.e(TAG, exception.message.toString())
        }
    }
}
