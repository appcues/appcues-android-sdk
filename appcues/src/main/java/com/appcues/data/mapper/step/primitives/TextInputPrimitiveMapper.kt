package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.styling.ComponentDataType
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextInputPrimitiveResponse

internal fun TextInputPrimitiveResponse.mapTextInputPrimitive() = TextInputPrimitive(
    id = id,
    style = style.mapComponentStyle(),
    label = label.mapTextPrimitive(),
    placeholder = placeholder,
    defaultValue = defaultValue,
    required = required ?: false,
    numberOfLines = numberOfLines ?: 1,
    maxLength = maxLength,
    dataType = mapComponentDataType(dataType),
    textFieldStyle = textFieldStyle.mapComponentStyle(),
)

private fun mapComponentDataType(value: String?) = when (value) {
    "text" -> ComponentDataType.TEXT
    "number" -> ComponentDataType.NUMBER
    "email" -> ComponentDataType.EMAIL
    "phone" -> ComponentDataType.PHONE
    "name" -> ComponentDataType.NAME
    "address" -> ComponentDataType.ADDRESS
    else -> ComponentDataType.TEXT
}