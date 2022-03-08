package com.appcues

internal data class AppcuesConfig(
    val accountId: String,
    val applicationId: String,
    val loggingLevel: Appcues.LoggingLevel,
    val apiHostUrl: String?,
    val anonymousIdFactory: (() -> String)?,
    val sessionTimeout: Int,
) {
    companion object {
        const val SESSION_TIMEOUT_DEFAULT = 1800 // 30 minutes by default
    }
}
