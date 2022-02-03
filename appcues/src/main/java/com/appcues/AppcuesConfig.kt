package com.appcues

import android.app.Activity

internal data class AppcuesConfig(
    val accountId: String,
    val applicationId: String,
    val loggingLevel: Appcues.LoggingLevel,
    val apiHostUrl: String?,
    val activity: Activity?,
)
