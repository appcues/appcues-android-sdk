package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionMapMapper
import com.appcues.data.mapper.styling.ContentModeMapper
import com.appcues.data.mapper.styling.SizeMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent.ImageComponent
import java.util.UUID

internal class ImageMapper(
    private val sizeMapper: SizeMapper = SizeMapper(),
    private val styleMapper: StyleMapper = StyleMapper(),
    private val contentModeMapper: ContentModeMapper = ContentModeMapper(),
    private val actionMapMapper: ActionMapMapper = ActionMapMapper(),
) {

    fun map(from: StepContentResponse, actions: HashMap<UUID, List<ActionResponse>>?): ImageComponent {
        return with(from) {
            requireNotNull(imageUrl) { throw AppcuesMappingException("image($id) imageUrl is null") }

            ImageComponent(
                id = id,
                url = imageUrl,
                accessibilityLabel = accessibilityLabel,
                style = styleMapper.map(style),
                intrinsicSize = intrinsicSize?.let { sizeMapper.map(it) },
                contentMode = contentModeMapper.map(contentMode),
                actions = actionMapMapper.map(actions, id)
            )
        }
    }
}
