package com.appcues.push

import com.appcues.analytics.AnalyticsEvent.PushOpened

internal data class PushOpenedAction(
    val notificationId: String,
    val userId: String,
    val eventProperties: Map<String, Any>?,
    val deeplink: String?,
    val experienceId: String?,
    val isTest: Boolean,
) {

    val eventName = PushOpened.eventName
}
