package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.SizeMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive.EmbedHtmlPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.EmbedPrimitiveResponse

internal class EmbedPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
    private val sizeMapper: SizeMapper = SizeMapper(),
) {

    fun map(from: EmbedPrimitiveResponse) = with(from) {
        EmbedHtmlPrimitive(
            id = id,
            style = styleMapper.map(style),
            embed = embed,
            intrinsicSize = intrinsicSize?.let { sizeMapper.map(it) },
        )
    }
}
