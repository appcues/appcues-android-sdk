package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.action.ActionsMapper
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepContentResponse
import java.util.UUID

internal class StackPrimitiveMapper(
    private val actionsMapper: ActionsMapper,
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(
        from: StepContentResponse,
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackTransform: (StepContentResponse) -> ExperiencePrimitive
    ): ExperiencePrimitive {
        requireNotNull(from.orientation) { throw AppcuesMappingException("stack(${from.id}) orientation is null") }

        return when (from.orientation) {
            "vertical" -> from.mapVerticalStack(actions, stackTransform)
            "horizontal" -> from.mapHorizontalStack(actions, stackTransform)
            else -> throw AppcuesMappingException("stack(${from.id}) unknown orientation ${from.orientation}")
        }
    }

    private fun StepContentResponse.mapVerticalStack(
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackItemsMapper: (StepContentResponse) -> ExperiencePrimitive,
    ) = VerticalStackPrimitive(
        id = id,
        items = items?.map { stackItemsMapper(it) } ?: arrayListOf(),
        spacing = spacing,
        style = styleMapper.map(style),
        actions = actionsMapper.map(actions, id)
    )

    private fun StepContentResponse.mapHorizontalStack(
        actions: HashMap<UUID, List<ActionResponse>>?,
        stackItemsMapper: (StepContentResponse) -> ExperiencePrimitive,
    ) = HorizontalStackPrimitive(
        id = id,
        items = items?.map { stackItemsMapper(it) } ?: arrayListOf(),
        distribution = distribution.toComponentDistribution(),
        spacing = spacing,
        style = styleMapper.map(style),
        actions = actionsMapper.map(actions, id)
    )

    private fun String?.toComponentDistribution(): ComponentDistribution {
        return when (this) {
            "center" -> ComponentDistribution.CENTER
            "equal" -> ComponentDistribution.EQUAL
            else -> ComponentDistribution.CENTER
        }
    }
}
