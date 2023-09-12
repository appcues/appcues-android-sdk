package com.appcues.analytics

import com.appcues.AnalyticType.EVENT
import com.appcues.AnalyticType.GROUP
import com.appcues.AnalyticType.IDENTIFY
import com.appcues.AnalyticType.SCREEN
import com.appcues.data.model.ExperienceStepFormState
import java.util.Date

internal fun AnalyticsActivity.getValue(): String? {
    return when (type) {
        IDENTIFY -> userId
        GROUP -> groupId
        EVENT -> eventName
        SCREEN -> eventName
    }
}

internal fun AnalyticsActivity.getProperties(): Map<String, Any>? {
    return when (type) {
        IDENTIFY -> profileProperties
        GROUP -> groupProperties
        EVENT -> eventAttributes.sanitize()
        SCREEN -> eventAttributes.sanitize()
    }
}

private fun Map<*, *>?.sanitize(): MutableMap<String, Any> {
    val sanitizedMap = mutableMapOf<String, Any>()

    this?.forEach {
        val key = it.key
        val value = it.value
        if (key is String && value != null) {
            sanitizedMap[key] = when (value) {
                is ExperienceStepFormState -> value.toHashMap().sanitize()
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
                is ExperienceStepFormState -> it.toHashMap().sanitize()
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
