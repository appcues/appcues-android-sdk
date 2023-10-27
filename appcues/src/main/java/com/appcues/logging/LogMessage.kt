package com.appcues.logging

import java.util.Date

internal data class LogMessage(
    val message: String,
    val type: LogType,
    val timestamp: Date
)
