package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.CustomComponentPrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.CustomComponentPrimitiveResponse

internal fun CustomComponentPrimitiveResponse.mapCustomViewPrimitive() = CustomComponentPrimitive(
    id = id,
    style = style.mapComponentStyle(),
    identifier = identifier,
    config = config
)
