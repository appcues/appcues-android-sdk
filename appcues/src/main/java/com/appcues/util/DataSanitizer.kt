package com.appcues.util

import com.appcues.data.model.ExperienceStepFormState
import org.jetbrains.annotations.VisibleForTesting
import java.util.Date
import java.util.UUID

internal class DataSanitizer {

    fun Map<*, *>.sanitize(): Map<String, Any> {
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

    @VisibleForTesting
    fun List<*>.sanitize(): List<Any> {
        val sanitizedList = mutableListOf<Any>()

        filterNotNull().forEach { item ->
            sanitizedList.add(
                when (item) {
                    is ExperienceStepFormState -> item.toHashMap().sanitize()
                    // convert Date types to Double value
                    is Date -> item.time.toDouble()
                    // convert UUID to string value
                    is UUID -> item.toString()
                    is Map<*, *> -> item.sanitize()
                    is List<*> -> item.sanitize()
                    else -> item
                }
            )
        }
        return sanitizedList
    }
}
