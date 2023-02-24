package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentColor
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive.OptionItem
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentDisplayFormat
import com.appcues.data.model.styling.ComponentSelectMode
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import kotlin.math.max
import kotlin.math.min

internal fun OptionSelectPrimitiveResponse.mapOptionSelectPrimitive(): OptionSelectPrimitive {
    // JSON data has Int values, we require UInt for these in the domain model
    val dataMin = (minSelections ?: 0).toUInt()
    val dataMax = maxSelections?.let { max(0, it).toUInt() }

    // the min cannot be more than the available options
    val trueMinSelections = min(dataMin, options.count().toUInt())

    // the max cannot be less than the min
    val trueMaxSelections = dataMax?.let { max(trueMinSelections, it) }

    return OptionSelectPrimitive(
        id = id,
        style = style.mapComponentStyle(),
        label = label.mapTextPrimitive(),
        errorLabel = errorLabel?.mapTextPrimitive(),
        selectMode = mapComponentSelectMode(selectMode),
        options = options.map { OptionItem(it.value, it.content.mapPrimitive(), it.selectedContent?.mapPrimitive()) },
        defaultValue = defaultValue ?: setOf(),
        minSelections = trueMinSelections,
        maxSelections = trueMaxSelections,
        controlPosition = mapComponentControlPosition(controlPosition),
        displayFormat = mapComponentDisplayFormat(displayFormat),
        pickerStyle = pickerStyle?.mapComponentStyle(),
        placeholder = placeholder?.mapPrimitive(),
        selectedColor = selectedColor?.mapComponentColor(),
        unselectedColor = unselectedColor?.mapComponentColor(),
        accentColor = accentColor?.mapComponentColor(),
        attributeName = attributeName,
        leadingFill = leadingFill ?: false
    )
}

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
    "nps" -> ComponentDisplayFormat.NPS
    else -> ComponentDisplayFormat.VERTICAL_LIST
}
