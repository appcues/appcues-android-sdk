package com.appcues.debugger

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.R
import com.appcues.analytics.AnalyticActivity
import com.appcues.analytics.ExperienceLifecycleEvent
import com.appcues.analytics.SdkMetrics
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.DebuggerEventItemPropertySection
import com.appcues.debugger.model.EventType
import com.appcues.debugger.ui.toEventTitle
import com.appcues.debugger.ui.toEventType
import com.appcues.util.ContextResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.Date

internal class DebuggerRecentEventsManager(
    private val contextResources: ContextResources,
) {

    companion object {

        private const val MAX_RECENT_EVENTS = 20
        private const val IDENTITY_PROPERTY_PREFIX = "_"
    }

    private val events: ArrayList<DebuggerEventItem> = arrayListOf()

    private var filterType: EventType? = null
    private val mutex = Mutex()

    private val _data = MutableSharedFlow<List<DebuggerEventItem>>(replay = 1)

    val data: MutableSharedFlow<List<DebuggerEventItem>>
        get() = _data

    private var lastEventId = 0

    suspend fun onActivity(activity: AnalyticActivity) = withContext(Dispatchers.IO) {
        when (activity.type) {
            IDENTIFY -> onActivityIdentify(activity)
            GROUP -> onActivityGroup(activity)
            EVENT -> onActivityEvent(activity)
            SCREEN -> onActivityEvent(activity)
        }

        updateData()
    }

    private fun onActivityIdentify(activity: AnalyticActivity) {
        events.addFirst(
            DebuggerEventItem(
                id = lastEventId,
                type = EventType.USER_PROFILE,
                timestamp = ((activity.profileProperties?.get("_updatedAt") as Date?) ?: Date()).time,
                name = activity.userId,
                propertySections = listOf(
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_properties_title),
                        properties = activity.profileProperties?.toSortedList(),
                    )
                )
            )
        )

        lastEventId++
    }

    private fun onActivityGroup(activity: AnalyticActivity) {
        events.addFirst(
            DebuggerEventItem(
                id = lastEventId,
                type = EventType.GROUP_UPDATE,
                timestamp = Date().time,
                name = activity.groupId ?: contextResources.getString(R.string.appcues_debugger_event_type_group_update_title),
                propertySections = listOf(
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_properties_title),
                        properties = activity.groupProperties?.toSortedList(),
                    )
                )
            )
        )
        lastEventId++
    }

    private fun onActivityEvent(activity: AnalyticActivity) {
        val type = activity.eventName.toEventType()
        val title = activity.eventName.toEventTitle()?.let { contextResources.getString(it) }
        val displayName = getEventDisplayName(activity, title)
        val attributes = activity.eventAttributes ?: hashMapOf()

        events.addFirst(
            DebuggerEventItem(
                id = lastEventId,
                type = type,
                timestamp = activity.timestamp.time,
                name = displayName,
                propertySections = listOf(
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_properties_title),
                        properties = attributes
                            .filterOutScreenProperties(type)
                            .filterOutAutoProperties()
                            .filterOutMetricsProperties()
                            .filterOutInteractionData()
                            .toSortedList(),
                    ),
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_form_response_title),
                        properties = attributes
                            .getFormResponse()
                            .toSortedList(),
                    ),
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_interaction_data),
                        properties = attributes
                            .getInteractionData()
                            .toSortedList(),
                    ),
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_identity_auto_properties_title),
                        properties = attributes
                            .getAutoProperties()
                            .toSortedList()
                    ),
                    DebuggerEventItemPropertySection(
                        title = contextResources.getString(R.string.appcues_debugger_event_details_sdk_metrics_properties_title),
                        properties = attributes
                            .getMetricsProperties()
                            .toSortedList()
                    )
                )
            )
        )

        lastEventId++
    }

    fun reset() {
        events.clear()
    }

    private fun getEventDisplayName(activity: AnalyticActivity, title: String?) =
        if (activity.type == SCREEN && activity.eventAttributes != null && activity.eventAttributes.contains("screenTitle")) {
            // screen views are a special case where the title is the screen title
            activity.eventAttributes["screenTitle"] as String
        } else {
            // otherwise - use the given title for system events or the event name for custom events
            title ?: (activity.eventName ?: String())
        }

    private fun Map<String, Any?>.toSortedList(): List<Pair<String, Any?>> = toList().let { list ->
        arrayListOf<Pair<String, Any?>>().apply {
            addAll(list.filter { it.first.startsWith(IDENTITY_PROPERTY_PREFIX).not() }.sortByPropertyName())
            addAll(list.filter { it.first.startsWith(IDENTITY_PROPERTY_PREFIX) }.sortByPropertyName())
        }
    }

    private fun ArrayList<DebuggerEventItem>.addFirst(element: DebuggerEventItem) {
        add(0, element)
    }

    suspend fun onApplyEventFilter(eventType: EventType?) = withContext(Dispatchers.IO) {
        filterType = eventType

        updateData()
    }

    private suspend fun updateData() = mutex.withLock {
        // this filtering of the `events` list is guarded with a Mutex since other threads
        // could update this list while we are processing through it
        val list = filterType?.let { eventType ->
            events.filter { it.type == eventType }.filterIndexed { index, _ -> index < MAX_RECENT_EVENTS }
        } ?: events.filterIndexed { index, _ -> index < MAX_RECENT_EVENTS }

        _data.emit(list)
    }
}

private fun Map<String, Any>.filterOutScreenProperties(eventType: EventType): Map<String, Any> {
    return if (eventType == EventType.SCREEN) {
        filterNot { it.key == "screenTitle" }
    } else this
}

private fun Map<String, Any>.filterOutAutoProperties(): Map<String, Any> {
    return filterNot { it.key == "_identify" }
}

private fun Map<String, Any>.filterOutMetricsProperties(): Map<String, Any> {
    return filterNot { it.key == SdkMetrics.METRICS_PROPERTY }
}

private fun Map<String, Any>.getAutoProperties(): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return (this["_identify"] as Map<String, Any>?) ?: mapOf()
}

private fun Map<String, Any>.getMetricsProperties(): Map<String, Any> {
    @Suppress("UNCHECKED_CAST")
    return (this[SdkMetrics.METRICS_PROPERTY] as Map<String, Any>?) ?: mapOf()
}

private fun Map<String, Any>.filterOutInteractionData(): Map<String, Any> {
    return filterNot { it.key == ExperienceLifecycleEvent.INTERACTION_DATA_KEY }
}

private fun Map<String, Any>.getFormResponse(): Map<String, Any?> {
    return (this[ExperienceLifecycleEvent.INTERACTION_DATA_KEY] as? ExperienceStepFormState)?.let {
        it.formItems.associate { itemState ->
            itemState.label to itemState.value
        }
    } ?: mapOf()
}

private fun Map<String, Any>.getInteractionData(): Map<String, Any?> {
    this[ExperienceLifecycleEvent.INTERACTION_DATA_KEY].let {
        if (it is ExperienceStepFormState || it == null) return mapOf()

        @Suppress("UNCHECKED_CAST")
        return it as Map<String, Any>
    }
}

private fun List<Pair<String, Any?>>.sortByPropertyName(): List<Pair<String, Any?>> = sortedBy { it.first }
