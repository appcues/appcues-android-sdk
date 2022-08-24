package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse

internal class StackPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(
        from: StackPrimitiveResponse,
        stackTransform: (PrimitiveResponse) -> ExperiencePrimitive
    ) = when (from.orientation) {
        "vertical" -> from.mapVerticalStack(stackTransform)
        "horizontal" -> from.mapHorizontalStack(stackTransform)
        else -> throw AppcuesMappingException("stack(${from.id}) unknown orientation ${from.orientation}")
    }

    private fun StackPrimitiveResponse.mapVerticalStack(
        stackItemsMapper: (PrimitiveResponse) -> ExperiencePrimitive,
    ) = VerticalStackPrimitive(
        id = id,
        items = items.map { stackItemsMapper(it) },
        spacing = spacing,
        style = styleMapper.map(style),
    )

    private fun StackPrimitiveResponse.mapHorizontalStack(
        stackItemsMapper: (PrimitiveResponse) -> ExperiencePrimitive,
    ) = HorizontalStackPrimitive(
        id = id,
        items = items.map { stackItemsMapper(it) },
        distribution = distribution.toComponentDistribution(),
        spacing = spacing,
        style = styleMapper.map(style),
    )

    private fun String?.toComponentDistribution(): ComponentDistribution {
        return when (this) {
            "center" -> ComponentDistribution.CENTER
            "equal" -> ComponentDistribution.EQUAL
            else -> ComponentDistribution.CENTER
        }
    }
}
