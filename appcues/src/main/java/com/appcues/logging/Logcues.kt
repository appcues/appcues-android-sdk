package com.appcues.logging

import android.util.Log
import com.appcues.Appcues

class Logcues(private val loggingLevel: Appcues.LoggingLevel) {

    companion object {
        private const val TAG = "Appcues"
    }

    fun i(message: String) {
        if (loggingLevel > Appcues.LoggingLevel.NONE) {
            Log.i(TAG, "$message")
        }
    }
}