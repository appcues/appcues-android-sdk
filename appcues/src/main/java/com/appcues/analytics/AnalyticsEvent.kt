package com.appcues.analytics

enum class AnalyticsEvent(val eventName: String) {
    ScreenView("appcues:screen_view"),
    SessionStarted("appcues:session_started"),
    SessionSuspended("appcues:session_suspended"),
    SessionResumed("appcues:session_resumed"),
    SessionReset("appcues:session_reset"),
}
