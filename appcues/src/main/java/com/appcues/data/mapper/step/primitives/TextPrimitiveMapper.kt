package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse

internal fun TextPrimitiveResponse.mapTextPrimitive() = TextPrimitive(
    id = id,
    text = text,
    style = style.mapComponentStyle(),
    localizable = localizable ?: true,
)
