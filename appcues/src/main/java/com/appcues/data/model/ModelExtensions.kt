package com.appcues.data.model

import com.appcues.data.mapper.styling.StyleColorMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.remote.response.styling.StyleColorResponse
import com.appcues.data.remote.response.styling.StyleResponse

typealias AppcuesConfigMap = HashMap<String, Any>?

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
        if (it is Double) it.toInt() else null
    }
}

internal fun AppcuesConfigMap.getConfigStyle(key: String): ComponentStyle? {
    return getConfig<Any>(key)?.let {
        StyleMapper().map(StyleResponse.fromAny(it))
    }
}

internal fun AppcuesConfigMap.getConfigColor(key: String): ComponentColor? {
    return getConfig<Any>(key)?.let {
        StyleColorMapper().map(StyleColorResponse.fromAny(it))
    }
}

internal fun Experience.areStepsFromDifferentContainers(stepIndexOne: Int, stepIndexTwo: Int): Boolean {
    return stepContainers.none { it.steps.containsAll(arrayListOf(getFlatStep(stepIndexOne), getFlatStep(stepIndexTwo))) }
}

internal fun Experience.getStepContainerIndex(stepIndex: Int): Int? {
    stepContainers.forEachIndexed { index, stepContainer -> if (stepContainer.steps.contains(getFlatStep(stepIndex))) return index }

    // should never reach because whoever is calling this should know
    // if stepIndex is a valid step. that being said,
    // stepContainer.steps.contains will always check true once
    return null
}

internal fun Experience.getFlatStep(index: Int): Step {
    return flatSteps[index]
}
