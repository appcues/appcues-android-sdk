package com.appcues.debugger

import com.appcues.R
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.AutoPropertyDecorator
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.EventType
import com.appcues.debugger.ui.toEventTitle
import com.appcues.debugger.ui.toEventType
import com.appcues.util.ContextResources
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private val _data = MutableStateFlow<List<DebuggerEventItem>>(arrayListOf())

    val data: StateFlow<List<DebuggerEventItem>>
        get() = _data

    suspend fun onActivityRequest(activityRequest: ActivityRequest) = withContext(Dispatchers.IO) {
        when {
            // event
            activityRequest.events != null -> {
                activityRequest.events.forEach { event ->
                    val type = event.name.toEventType()
                    val title = event.name.toEventTitle()?.let { contextResources.getString(it) } ?: event.name

                    events.addFirst(
                        DebuggerEventItem(
                            type = type,
                            title = title,
                            timestamp = event.timestamp.time,
                            name = getEventName(event, type, title),
                            properties = event.attributes
                                .filterOutScreenProperties(type)
                                .filterOutAutoProperties()
                                .toSortedList(),
                            identityProperties = event.attributes
                                .getAutoProperties()
                                .toSortedList()
                        )
                    )
                }
            }
            // group update
            activityRequest.groupUpdate != null -> {
                events.addFirst(
                    DebuggerEventItem(
                        type = EventType.GROUP_UPDATE,
                        title = contextResources.getString(R.string.debugger_event_type_group_update_title),
                        timestamp = Date().time,
                        name = activityRequest.groupId ?: contextResources.getString(R.string.debugger_event_type_group_update_title),
                        properties = activityRequest.groupUpdate.toSortedList(),
                        identityProperties = null
                    )
                )
            }
            // profile update
            activityRequest.profileUpdate != null -> {
                events.addFirst(
                    DebuggerEventItem(
                        type = EventType.USER_PROFILE,
                        title = contextResources.getString(R.string.debugger_event_type_profile_update_title),
                        // it should always contain updated at property, this is just a safeguard
                        // in case something changes in the future to avoid unwanted exceptions
                        timestamp = (activityRequest.profileUpdate[AutoPropertyDecorator.UPDATED_AT_PROPERTY] as Long?) ?: Date().time,
                        name = activityRequest.userId,
                        properties = activityRequest.profileUpdate.toSortedList(),
                        identityProperties = null
                    )
                )
            }
        }

        updateData()
    }

    private fun Map<String, Any>.filterOutScreenProperties(eventType: EventType): Map<String, Any> {
        return if (eventType == EventType.SCREEN) {
            filterNot { it.key == ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE }
        } else this
    }

    private fun Map<String, Any>.filterOutAutoProperties(): Map<String, Any> {
        return filterNot { it.key == AutoPropertyDecorator.IDENTITY_PROPERTY }
    }

    private fun Map<String, Any>.getAutoProperties(): Map<String, Any> {
        @Suppress("UNCHECKED_CAST")
        return (this[AutoPropertyDecorator.IDENTITY_PROPERTY] as Map<String, Any>?) ?: mapOf()
    }

    private fun getEventName(
        event: EventRequest,
        type: EventType,
        title: String
    ) = if (type == EventType.SCREEN && event.attributes.contains(ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE))
        event.attributes[ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE] as String
    else title

    private fun Map<String, Any>.toSortedList(): List<Pair<String, Any>> = toList().let { list ->
        arrayListOf<Pair<String, Any>>().apply {
            addAll(list.filter { it.first.startsWith(IDENTITY_PROPERTY_PREFIX).not() }.sortByPropertyName())
            addAll(list.filter { it.first.startsWith(IDENTITY_PROPERTY_PREFIX) }.sortByPropertyName())
        }
    }

    private fun List<Pair<String, Any>>.sortByPropertyName(): List<Pair<String, Any>> = sortedBy { it.first }

    private fun ArrayList<DebuggerEventItem>.addFirst(element: DebuggerEventItem) {
        add(0, element)
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
