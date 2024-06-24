package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.CustomFramePrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.CustomViewPrimitiveResponse

internal fun CustomViewPrimitiveResponse.mapCustomViewPrimitive() = CustomFramePrimitive(
    id = id,
    style = style.mapComponentStyle(),
    identifier = identifier,
    config = config
)
