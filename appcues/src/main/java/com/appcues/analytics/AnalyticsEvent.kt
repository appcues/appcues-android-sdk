package com.appcues.analytics

internal enum class AnalyticsEvent(val eventName: String) {
    ScreenView("appcues:screen_view"),
    SessionStarted("appcues:session_started"),
    ExperienceStepSeen("appcues:v2:step_seen"),
    ExperienceStepInteraction("appcues:v2:step_interaction"),
    ExperienceStepCompleted("appcues:v2:step_completed"),
    ExperienceStepError("appcues:v2:step_error"),
    ExperienceStepRecovered("appcues:v2:step_recovered"),
    ExperienceStarted("appcues:v2:experience_started"),
    ExperienceCompleted("appcues:v2:experience_completed"),
    ExperienceDismissed("appcues:v2:experience_dismissed"),
    ExperienceError("appcues:v2:experience_error"),
    ExperienceRecovery("appcues:v2:experience_recovered"),
    ExperimentEntered("appcues:experiment_entered"),
    DeviceUpdated("appcues:device_updated"),
    DeviceUnregistered("appcues:device_unregistered"),
    PushOpened("appcues:push_opened"),
}
