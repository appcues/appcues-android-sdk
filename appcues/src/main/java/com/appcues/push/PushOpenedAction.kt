package com.appcues.push

import com.appcues.analytics.AnalyticsEvent.PushOpened

internal data class PushOpenedAction(
    val userId: String,
    val eventProperties: Map<String, Any>?,
    val deeplink: String?,
    val experienceId: String?,
) {

    val eventName = PushOpened.eventName
}
