package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.BoxPrimitiveResponse

internal fun BoxPrimitiveResponse.mapBoxPrimitive() = BoxPrimitive(
    id = id,
    items = items.map { it.mapPrimitive() },
    style = style.mapComponentStyle(),
)
