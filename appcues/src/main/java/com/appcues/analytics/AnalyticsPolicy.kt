package com.appcues.analytics

import com.appcues.SessionMonitor

internal class AnalyticsPolicy(
    private val sessionMonitor: SessionMonitor,
) {

    fun canIdentify() = basicAnalyticsPolicy("unable to track user")

    fun canTrackEvent() = basicAnalyticsPolicy("unable to track event")

    fun canTrackScreen() = basicAnalyticsPolicy("unable to track screen")

    fun canTrackGroup() = basicAnalyticsPolicy("unable to track group")

    private fun basicAnalyticsPolicy(message: String) = sessionMonitor.checkSession(message)
}
