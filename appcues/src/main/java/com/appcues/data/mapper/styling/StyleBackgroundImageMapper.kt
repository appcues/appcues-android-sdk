package com.appcues.data.mapper.styling

import com.appcues.data.model.styling.ComponentBackgroundImage
import com.appcues.data.remote.response.styling.StyleBackgroundImageResponse

internal class StyleBackgroundImageMapper(
    private val sizeMapper: SizeMapper = SizeMapper(),
    private val contentModeMapper: ContentModeMapper = ContentModeMapper(),
) {

    fun map(from: StyleBackgroundImageResponse?): ComponentBackgroundImage? {
        if (from == null) return null

        return ComponentBackgroundImage(
            imageUrl = from.imageUrl,
            blurHash = from.blurHash,
            intrinsicSize = from.intrinsicSize?.let { sizeMapper.map(it) },
            contentMode = contentModeMapper.map(from.contentMode),
            verticalAlignment = from.verticalAlignment.toComponentVerticalAlignment(),
            horizontalAlignment = from.horizontalAlignment.toComponentHorizontalAlignment(),
        )
    }
}
