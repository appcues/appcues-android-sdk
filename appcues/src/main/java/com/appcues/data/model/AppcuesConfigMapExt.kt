package com.appcues.data.model

import com.appcues.action.ActionRegistry
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.action.toAction
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.mapper.styling.mapComponentColor
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.remote.appcues.response.action.ActionResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.styling.StyleColorResponse
import com.appcues.data.remote.appcues.response.styling.StyleResponse

internal typealias AppcuesConfigMap = Map<String, Any>?

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
    return get(key) as? T
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
        MoshiConfiguration.fromAny<StyleResponse>(it).mapComponentStyle()
    }
}

internal fun AppcuesConfigMap.getConfigPrimitive(key: String): ExperiencePrimitive? {
    return getConfig<Any>(key)?.let {
        MoshiConfiguration.fromAny<PrimitiveResponse>(it)?.mapPrimitive()
    }
}

internal fun AppcuesConfigMap.getConfigActions(key: String, actionRegistry: ActionRegistry, renderContext: RenderContext): List<Action> {
    return getConfig<List<Any>>(key)?.map { MoshiConfiguration.fromAny<ActionResponse>(it) }
        ?.mapNotNull { response -> response?.toAction(actionRegistry, renderContext) } ?: arrayListOf()
}

internal fun AppcuesConfigMap.getConfigColor(key: String): ComponentColor? {
    return getConfig<Any>(key)?.let {
        MoshiConfiguration.fromAny<StyleColorResponse>(it)?.mapComponentColor()
    }
}

// general helper to get any object of type T from the config map and have it deserialized from JSON
// using it's Moshi adapter
internal inline fun <reified T : Any> AppcuesConfigMap.getConfigObject(key: String): T? {
    return getConfig<Any>(key)?.let {
        MoshiConfiguration.fromAny(it)
    }
}
