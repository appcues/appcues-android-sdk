package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.ContentModeMapper
import com.appcues.data.mapper.styling.SizeMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.ImagePrimitiveResponse

internal class ImagePrimitiveMapper(
    private val sizeMapper: SizeMapper = SizeMapper(),
    private val styleMapper: StyleMapper = StyleMapper(),
    private val contentModeMapper: ContentModeMapper = ContentModeMapper(),
) {

    fun map(from: ImagePrimitiveResponse) = with(from) {
        ImagePrimitive(
            id = id,
            url = imageUrl,
            accessibilityLabel = accessibilityLabel,
            style = styleMapper.map(style),
            intrinsicSize = intrinsicSize?.let { sizeMapper.map(it) },
            contentMode = contentModeMapper.map(contentMode),
            blurHash = blurHash,
        )
    }
}
