package com.appcues.debugger

import com.appcues.analytics.AnalyticsEvent
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.EventType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date

internal class DebuggerRecentEventsManager {

    companion object {

        private const val MAX_RECENT_EVENTS = 20
    }

    private val events: ArrayList<DebuggerEventItem> = arrayListOf()

    private var filterType: EventType? = null

    private val _data = MutableStateFlow<List<DebuggerEventItem>>(arrayListOf())

    val data: StateFlow<List<DebuggerEventItem>>
        get() = _data

    suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        val dateFormat = SimpleDateFormat("hh:mm:ss", java.util.Locale.getDefault())
        when {
            // event
            activityRequest.events != null -> {
                activityRequest.events.forEach { event ->
                    events.addFirst(
                        DebuggerEventItem(
                            type = event.name.toEventType(),
                            title = event.name.toEventTitle(),
                            timestamp = dateFormat.format(event.timestamp),
                            properties = event.attributes,
                        )
                    )
                }
            }
            // group update
            activityRequest.groupUpdate != null -> {
                events.addFirst(
                    DebuggerEventItem(
                        type = EventType.GROUP_UPDATE,
                        title = "Group Update",
                        timestamp = dateFormat.format(Date()),
                        properties = activityRequest.groupUpdate
                    )
                )
            }
            // profile update
            activityRequest.profileUpdate != null -> {
                events.addFirst(
                    DebuggerEventItem(
                        type = EventType.USER_PROFILE,
                        title = "Profile Update",
                        // it should always contain _updatedAt property, this is just a safeguard
                        // in case something changes in the future to avoid unwanted exceptions
                        timestamp = dateFormat.format((activityRequest.profileUpdate["_updatedAt"] as Long?)?.let { Date(it) } ?: Date()),
                        properties = activityRequest.profileUpdate
                    )
                )
            }
        }

        updateData()
    }

    private fun ArrayList<DebuggerEventItem>.addFirst(element: DebuggerEventItem) {
        add(0, element)
    }

    private fun String.toEventType(): EventType = when (this) {
        AnalyticsEvent.ScreenView.eventName -> EventType.SCREEN
        AnalyticsEvent.SessionStarted.eventName,
        AnalyticsEvent.SessionSuspended.eventName,
        AnalyticsEvent.SessionResumed.eventName,
        AnalyticsEvent.SessionReset.eventName -> EventType.SESSION
        AnalyticsEvent.ExperienceStepSeen.eventName,
        AnalyticsEvent.ExperienceStepInteraction.eventName,
        AnalyticsEvent.ExperienceStepCompleted.eventName,
        AnalyticsEvent.ExperienceStepError.eventName,
        AnalyticsEvent.ExperienceStepRecovered.eventName,
        AnalyticsEvent.ExperienceStarted.eventName,
        AnalyticsEvent.ExperienceCompleted.eventName,
        AnalyticsEvent.ExperienceDismissed.eventName,
        AnalyticsEvent.ExperienceError.eventName -> EventType.EXPERIENCE
        else -> EventType.CUSTOM
    }

    private fun String.toEventTitle(): String = when (this) {
        AnalyticsEvent.ScreenView.eventName -> "Screen View"
        AnalyticsEvent.SessionStarted.eventName -> "Session Started"
        AnalyticsEvent.SessionSuspended.eventName -> "Session Suspended"
        AnalyticsEvent.SessionResumed.eventName -> "Session Resumed"
        AnalyticsEvent.SessionReset.eventName -> "Session Reset"
        AnalyticsEvent.ExperienceStepSeen.eventName -> "Step Seen"
        AnalyticsEvent.ExperienceStepInteraction.eventName -> "Step Interaction"
        AnalyticsEvent.ExperienceStepCompleted.eventName -> "Step Completed"
        AnalyticsEvent.ExperienceStepError.eventName -> "Step Error"
        AnalyticsEvent.ExperienceStepRecovered.eventName -> "Step Recovered"
        AnalyticsEvent.ExperienceStarted.eventName -> "Experience Started"
        AnalyticsEvent.ExperienceCompleted.eventName -> "Experience Completed"
        AnalyticsEvent.ExperienceDismissed.eventName -> "Experience Dismissed"
        AnalyticsEvent.ExperienceError.eventName -> "Experience Error"
        else -> "Event $this"
    }

    suspend fun onApplyEventFilter(eventType: EventType?) = withContext(Dispatchers.IO) {
        filterType = eventType

        updateData()
    }

    private suspend fun updateData() {
        val list = filterType?.let { eventType ->
            events.filter { it.type == eventType }.filterIndexed { index, _ -> index < MAX_RECENT_EVENTS }
        } ?: events.filterIndexed { index, _ -> index < MAX_RECENT_EVENTS }

        _data.emit(list)
    }
}
