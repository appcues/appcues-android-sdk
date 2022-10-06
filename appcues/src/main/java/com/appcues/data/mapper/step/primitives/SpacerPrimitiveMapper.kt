package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.SpacerPrimitiveResponse

internal fun SpacerPrimitiveResponse.mapSpacerPrimitive() = SpacerPrimitive(
    id = id,
    style = style.mapComponentStyle(),
)
