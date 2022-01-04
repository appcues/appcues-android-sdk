package com.appcues

internal data class AppcuesConfig(
    val accountId: String,
    val applicationId: String,
    val loggingLevel: Appcues.LoggingLevel,
)