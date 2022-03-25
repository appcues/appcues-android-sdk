package com.appcues

internal data class AppcuesConfig(
    val accountId: String,
    val applicationId: String,
    val loggingLevel: Appcues.LoggingLevel,
    val apiHostUrl: String?,
    val anonymousIdFactory: (() -> String)?,
    val sessionTimeout: Int,
    val activityStorageMaxSize: Int,
    val activityStorageMaxAge: Int?,
    var interceptor: AppcuesInterceptor?,
    var experienceListener: ExperienceListener?,
) {
    companion object {
        const val SESSION_TIMEOUT_DEFAULT = 1800 // 30 minutes by default
        const val ACTIVITY_STORAGE_MAX_SIZE = 25
    }
}
