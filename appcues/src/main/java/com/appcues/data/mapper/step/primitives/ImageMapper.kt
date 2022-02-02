package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.SizeMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent.ImageComponent

internal class ImageMapper(
    private val sizeMapper: SizeMapper = SizeMapper(),
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(from: StepContentResponse): ImageComponent {
        return with(from) {
            requireNotNull(imageUrl) { throw AppcuesMappingException("image($id) imageUrl is null") }

            ImageComponent(
                id = id,
                url = imageUrl,
                accessibilityLabel = accessibilityLabel,
                style = styleMapper.map(style),
                intrinsicSize = intrinsicSize?.let { sizeMapper.map(it) }
            )
        }
    }
}
