package com.appcues

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.analytics.ActivityRequestBuilder
import com.appcues.analytics.TrackingData
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.remote.appcues.request.EventRequest
import java.util.Date
import java.util.UUID

internal class AnalyticsPublisher(
    private val storage: Storage
) {

    fun publish(listener: AnalyticsListener?, data: TrackingData) {
        if (listener == null) return

        when (data.type) {
            EVENT -> data.request.events?.forEach {
                listener.trackedAnalytic(EVENT, it.name, it.attributes.sanitize(), data.isInternal)
            }
            IDENTIFY -> listener.trackedAnalytic(IDENTIFY, storage.userId, data.request.profileUpdate?.sanitize(), data.isInternal)
            GROUP -> listener.trackedAnalytic(GROUP, storage.groupId, data.request.groupUpdate?.sanitize(), data.isInternal)
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
                    is ExperienceStepFormState -> value.toHashMap().sanitize()
                    // convert Date types to Double value
                    is Date -> value.time.toDouble()
                    // convert UUID to string value
                    is UUID -> value.toString()
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
                    is ExperienceStepFormState -> it.toHashMap().sanitize()
                    // convert Date types to Double value
                    is Date -> it.time.toDouble()
                    // convert UUID to string value
                    is UUID -> it.toString()
                    is Map<*, *> -> it.sanitize()
                    is List<*> -> it.sanitize()
                    else -> it
                }
            )
        }
        return sanitizedList
    }
}
