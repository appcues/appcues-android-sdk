package com.appcues.data.model

import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.step.StepContentMapper
import com.appcues.data.mapper.styling.StyleColorMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.remote.response.step.StepContentResponse

typealias AppcuesConfigMap = Map<String, Any>?

internal inline fun <reified T : Any?> AppcuesConfigMap.getConfigOrDefault(key: String, default: T): T {
    // if hash config is null, return default
    if (this == null) return default
    // get value by key else return default
    return getConfig<T>(key) ?: default
}

internal inline fun <reified T : Any?> AppcuesConfigMap.getConfig(key: String): T? {
    // if hash config is null, return default
    if (this == null) return null
    // get value by key else return default
    return get(key)?.let {
        // if is instance of T return it else return default
        if (it is T) it else null
    }
}

internal fun AppcuesConfigMap.getConfigInt(key: String): Int? {
    // if hash config is null, return default
    if (this == null) return null
    // get value by key as Int?
    return get(key)?.let {
        when (it) {
            is Double -> it.toInt()
            is Int -> it
            else -> null
        }
    }
}

internal fun AppcuesConfigMap.getConfigStyle(key: String): ComponentStyle? {
    return getConfig<Any>(key)?.let {
        StyleMapper().map(MoshiConfiguration.fromAny(it))
    }
}

internal fun AppcuesConfigMap.getConfigPrimitive(key: String, stepContentMapper: StepContentMapper): ExperiencePrimitive? {
    return getConfig<Any>(key)?.let {
        val stepContentResponse = MoshiConfiguration.fromAny<StepContentResponse>(it)
        return if (stepContentResponse != null) {
            stepContentMapper.map(stepContentResponse)
        } else null
    }
}

internal fun AppcuesConfigMap.getConfigColor(key: String): ComponentColor? {
    return getConfig<Any>(key)?.let {
        StyleColorMapper().map(MoshiConfiguration.fromAny(it))
    }
}
