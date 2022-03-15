package com.appcues.analytics

enum class AnalyticEvents(val eventName: String) {
    ScreenView("appcues:screen_view"),
    SessionStarted("appcues:session_started"),
    SessionSuspended("appcues:session_suspended"),
    SessionResumed("appcues:session_resumed"),
    SessionReset("appcues:session_reset"),
    StepSeen("appcues:v2:step_seen"),
    StepInteraction("appcues:v2:step_interaction"),
    StepCompleted("appcues:v2:step_completed"),
    StepError("appcues:v2:step_error"),
    StepRecovered("appcues:v2:step_recovered"),
    ExperienceStarted("appcues:v2:experience_started"),
    ExperienceCompleted("appcues:v2:experience_completed"),
    ExperienceDismissed("appcues:v2:experience_dismissed"),
    ExperienceError("appcues:v2:experience_error"),
}
