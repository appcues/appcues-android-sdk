package com.appcues.push

import com.appcues.analytics.AnalyticsEvent.PushOpened
import java.util.UUID

internal data class PushOpenedAction(
    val pushNotificationId: UUID,
    val userId: String,
    val eventProperties: Map<String, Any>?,
    val deeplink: String?,
    val experienceId: String?,
    val isTest: Boolean,
) {

    val eventName = PushOpened.eventName
}
