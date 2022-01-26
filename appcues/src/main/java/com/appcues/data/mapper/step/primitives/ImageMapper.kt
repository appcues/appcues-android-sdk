package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.SizeMapper
import com.appcues.data.mapper.styling.StyleColorMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.data.remote.response.styling.StyleResponse
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import com.appcues.domain.entity.styling.ComponentColor

internal class ImageMapper(
    private val sizeMapper: SizeMapper = SizeMapper(),
    private val styleMapper: StyleMapper = StyleMapper(),
    private val styleColorMapper: StyleColorMapper = StyleColorMapper(),
) {

    fun map(from: StepContentResponse): ImageComponent {
        return with(from) {
            requireNotNull(intrinsicSize) { throw AppcuesMappingException("image($id) intrinsicSize is null") }
            requireNotNull(imageUrl) { throw AppcuesMappingException("image($id) imageUrl is null") }

            ImageComponent(
                id = id,
                url = imageUrl,
                size = sizeMapper.map(intrinsicSize),
                backgroundColor = style.getBackgroundColor(),
                style = styleMapper.map(style)
            )
        }
    }

    private fun StyleResponse?.getBackgroundColor(): ComponentColor? {
        return if (this == null || backgroundColor == null) null else {
            styleColorMapper.map(backgroundColor)
        }
    }
}
