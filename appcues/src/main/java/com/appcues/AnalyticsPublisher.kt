package com.appcues

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.TrackingData
import com.appcues.data.remote.request.EventRequest
import java.util.Date

internal class AnalyticsPublisher(
    private val storage: Storage
) {

    fun publish(listener: AnalyticsListener?, data: TrackingData) {
        if (listener == null) return

        when (data.type) {
            EVENT -> data.request.events?.forEach {
                listener.trackedAnalytic(EVENT, it.name, it.attributes.sanitize(), data.isInternal)
            }
            IDENTIFY -> listener.trackedAnalytic(IDENTIFY, storage.userId, data.request.profileUpdate, data.isInternal)
            GROUP -> listener.trackedAnalytic(GROUP, storage.groupId, data.request.groupUpdate, data.isInternal)
            SCREEN -> data.request.events?.forEach {
                listener.trackedAnalytic(SCREEN, it.screenTitle(), it.attributes.sanitize(), data.isInternal)
            }
        }
    }

    private fun EventRequest.screenTitle(): String? =
        attributes[ActivityRequestBuilder.SCREEN_TITLE_ATTRIBUTE] as? String

    private fun Map<*, *>.sanitize(): MutableMap<String, Any> {
        val sanitizedMap = mutableMapOf<String, Any>()

        forEach {
            val key = it.key
            val value = it.value
            if (key is String && value != null) {
                sanitizedMap[key] = when (value) {
                    // convert Date types to Double value
                    is Date -> value.time.toDouble()
                    is Map<*, *> -> value.sanitize()
                    is List<*> -> value.sanitize()
                    else -> value
                }
            }
        }
        return sanitizedMap
    }

    private fun List<*>.sanitize(): List<*> {
        val sanitizedList = mutableListOf<Any>()

        filterNotNull().forEach {
            sanitizedList.add(
                when (it) {
                    // convert Date types to Double value
                    is Date -> it.time.toDouble()
                    is Map<*, *> -> it.sanitize()
                    is List<*> -> it.sanitize()
                    else -> it
                }
            )
        }
        return sanitizedList
    }
}
