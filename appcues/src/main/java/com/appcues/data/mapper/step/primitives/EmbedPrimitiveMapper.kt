package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.mapComponentSize
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse

internal fun EmbedPrimitiveResponse.mapEmbedPrimitive() = EmbedHtmlPrimitive(
    id = id,
    style = style.mapComponentStyle(),
    embed = embed,
    intrinsicSize = intrinsicSize.mapComponentSize(),
)
