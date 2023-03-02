package com.appcues.data.mapper.step.primitives

import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.SpacerPrimitiveResponse

internal fun SpacerPrimitiveResponse.mapSpacerPrimitive() = SpacerPrimitive(
    id = id,
    spacing = spacing,
)
