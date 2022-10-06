package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ButtonPrimitiveResponse

internal fun ButtonPrimitiveResponse.mapButtonPrimitive() = ButtonPrimitive(
    id = id,
    content = content.mapPrimitive(),
    style = style.mapComponentStyle(),
)
