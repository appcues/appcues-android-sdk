package com.appcues.debugger.ui

import com.appcues.R
import com.appcues.analytics.AnalyticsEvent
import com.appcues.debugger.model.EventType
import com.appcues.debugger.model.EventType.CUSTOM
import com.appcues.debugger.model.EventType.DEVICE
import com.appcues.debugger.model.EventType.EXPERIENCE
import com.appcues.debugger.model.EventType.GROUP_UPDATE
import com.appcues.debugger.model.EventType.SCREEN
import com.appcues.debugger.model.EventType.SESSION
import com.appcues.debugger.model.EventType.USER_PROFILE

internal fun EventType?.getTitleString(): Int {
    return when (this) {
        EXPERIENCE -> R.string.appcues_debugger_recent_events_filter_experience
        GROUP_UPDATE -> R.string.appcues_debugger_recent_events_filter_group
        USER_PROFILE -> R.string.appcues_debugger_recent_events_filter_profile
        CUSTOM -> R.string.appcues_debugger_recent_events_filter_custom
        SCREEN -> R.string.appcues_debugger_recent_events_filter_screen
        SESSION -> R.string.appcues_debugger_recent_events_filter_session
        else -> R.string.appcues_debugger_recent_events_filter_all
    }
}

internal fun EventType?.toResourceId(): Int {
    return when (this) {
        EXPERIENCE -> R.drawable.appcues_ic_experience
        GROUP_UPDATE -> R.drawable.appcues_ic_group
        USER_PROFILE -> R.drawable.appcues_ic_user_profile
        CUSTOM -> R.drawable.appcues_ic_custom
        SCREEN -> R.drawable.appcues_ic_screen
        SESSION -> R.drawable.appcues_ic_session
        DEVICE -> R.drawable.appcues_ic_device
        else -> R.drawable.appcues_ic_all
    }
}

internal fun String.toEventType(): EventType = when (this) {
    AnalyticsEvent.ScreenView.eventName -> SCREEN
    AnalyticsEvent.SessionStarted.eventName -> SESSION
    AnalyticsEvent.DeviceUpdated.eventName,
    AnalyticsEvent.DeviceUpdated.eventName -> DEVICE
    AnalyticsEvent.ExperienceStepSeen.eventName,
    AnalyticsEvent.ExperienceStepInteraction.eventName,
    AnalyticsEvent.ExperienceStepCompleted.eventName,
    AnalyticsEvent.ExperienceStepError.eventName,
    AnalyticsEvent.ExperienceStepRecovered.eventName,
    AnalyticsEvent.ExperienceStarted.eventName,
    AnalyticsEvent.ExperienceCompleted.eventName,
    AnalyticsEvent.ExperienceDismissed.eventName,
    AnalyticsEvent.ExperienceError.eventName -> EXPERIENCE
    else -> CUSTOM
}

internal fun String.toEventTitle(): Int? = when (this) {
    AnalyticsEvent.SessionStarted.eventName -> R.string.appcues_debugger_event_type_session_started_title
    AnalyticsEvent.DeviceUpdated.eventName -> R.string.appcues_debugger_event_type_device_updated_title
    AnalyticsEvent.DeviceUnregistered.eventName -> R.string.appcues_debugger_event_type_device_unregistered_title
    AnalyticsEvent.ExperienceStepSeen.eventName -> R.string.appcues_debugger_event_type_step_seen_title
    AnalyticsEvent.ExperienceStepInteraction.eventName -> R.string.appcues_debugger_event_type_step_interaction_title
    AnalyticsEvent.ExperienceStepCompleted.eventName -> R.string.appcues_debugger_event_type_step_completed_title
    AnalyticsEvent.ExperienceStepError.eventName -> R.string.appcues_debugger_event_type_step_error_title
    AnalyticsEvent.ExperienceStepRecovered.eventName -> R.string.appcues_debugger_event_type_step_recovered_title
    AnalyticsEvent.ExperienceStarted.eventName -> R.string.appcues_debugger_event_type_experience_started_title
    AnalyticsEvent.ExperienceCompleted.eventName -> R.string.appcues_debugger_event_type_experience_completed_title
    AnalyticsEvent.ExperienceDismissed.eventName -> R.string.appcues_debugger_event_type_experience_dismissed_title
    AnalyticsEvent.ExperienceError.eventName -> R.string.appcues_debugger_event_type_experience_error_title
    AnalyticsEvent.ExperienceRecovery.eventName -> R.string.appcues_debugger_event_type_experience_recover_title
    AnalyticsEvent.PushOpened.eventName -> R.string.appcues_debugger_event_type_push_opened_title
    else -> null
}
