package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.step.mapPrimitive
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive.OptionItem
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentDisplayFormat
import com.appcues.data.model.styling.ComponentSelectMode
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse

internal fun OptionSelectPrimitiveResponse.mapOptionSelectPrimitive() = OptionSelectPrimitive(
    id = id,
    style = style.mapComponentStyle(),
    label = label.mapTextPrimitive(),
    selectMode = mapComponentSelectMode(selectMode),
    options = options.map { OptionItem(it.value, it.content.mapPrimitive(), it.selectedContent?.mapPrimitive()) },
    defaultValue = defaultValue ?: setOf(),
    required = required ?: false,
    controlPosition = mapComponentControlPosition(controlPosition),
    displayFormat = mapComponentDisplayFormat(displayFormat),
)

private fun mapComponentSelectMode(value: String) = when (value) {
    "single" -> ComponentSelectMode.SINGLE
    "multi" -> ComponentSelectMode.MULTIPLE
    else -> ComponentSelectMode.SINGLE
}

private fun mapComponentControlPosition(value: String?) = when (value) {
    "leading" -> ComponentControlPosition.LEADING
    "trailing" -> ComponentControlPosition.TRAILING
    "top" -> ComponentControlPosition.TOP
    "bottom" -> ComponentControlPosition.BOTTOM
    "hidden" -> ComponentControlPosition.HIDDEN
    else -> ComponentControlPosition.LEADING
}

private fun mapComponentDisplayFormat(value: String?) = when (value) {
    "verticalList" -> ComponentDisplayFormat.VERTICAL_LIST
    "horizontalList" -> ComponentDisplayFormat.HORIZONTAL_LIST
    "picker" -> ComponentDisplayFormat.PICKER
    else -> ComponentDisplayFormat.VERTICAL_LIST
}
