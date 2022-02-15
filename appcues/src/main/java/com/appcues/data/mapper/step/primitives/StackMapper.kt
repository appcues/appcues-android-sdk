package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionMapMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import com.appcues.domain.entity.ExperienceComponent
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.styling.ComponentDistribution
import java.util.UUID

internal class StackMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
    private val actionMapMapper: ActionMapMapper = ActionMapMapper(),
) {

    fun map(
        from: StepContentResponse,
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackTransform: (StepContentResponse) -> ExperienceComponent
    ): ExperienceComponent {
        requireNotNull(from.orientation) { throw AppcuesMappingException("stack(${from.id}) orientation is null") }

        return when (from.orientation) {
            "vertical" -> from.mapVerticalStack(actions, stackTransform)
            "horizontal" -> from.mapHorizontalStack(actions, stackTransform)
            else -> throw AppcuesMappingException("stack(${from.id}) unknown orientation ${from.orientation}")
        }
    }

    private fun StepContentResponse.mapVerticalStack(
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackItemsMapper: (StepContentResponse) -> ExperienceComponent,
    ): VerticalStackComponent {
        return VerticalStackComponent(
            id = id,
            items = items?.map { stackItemsMapper(it) } ?: arrayListOf(),
            spacing = spacing,
            style = styleMapper.map(style),
            actions = actionMapMapper.map(actions, id)
        )
    }

    private fun StepContentResponse.mapHorizontalStack(
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackItemsMapper: (StepContentResponse) -> ExperienceComponent,
    ): HorizontalStackComponent {
        return HorizontalStackComponent(
            id = id,
            items = items?.map { stackItemsMapper(it) } ?: arrayListOf(),
            distribution = distribution.toComponentDistribution(),
            spacing = spacing,
            style = styleMapper.map(style),
            actions = actionMapMapper.map(actions, id)
        )
    }

    private fun String?.toComponentDistribution(): ComponentDistribution {
        return when (this) {
            "center" -> ComponentDistribution.CENTER
            "equal" -> ComponentDistribution.EQUAL
            else -> ComponentDistribution.CENTER
        }
    }
}
