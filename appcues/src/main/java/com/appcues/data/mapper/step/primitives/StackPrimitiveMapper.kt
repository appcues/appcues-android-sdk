package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse

internal fun StackPrimitiveResponse.mapStackPrimitive() = when (orientation) {
    "vertical" -> mapVerticalStack()
    "horizontal" -> mapHorizontalStack()
    else -> throw AppcuesMappingException("stack($id) unknown orientation $orientation")
}

private fun StackPrimitiveResponse.mapVerticalStack(): VerticalStackPrimitive {
    return VerticalStackPrimitive(
        id = id,
        items = items.map { it.mapPrimitive() },
        spacing = spacing,
        style = style.mapComponentStyle(),
    )
}

private fun StackPrimitiveResponse.mapHorizontalStack(): HorizontalStackPrimitive {
    return HorizontalStackPrimitive(
        id = id,
        items = items.map { it.mapPrimitive() },
        distribution = mapComponentDistribution(distribution),
        spacing = spacing,
        style = style.mapComponentStyle(),
    )
}

private fun mapComponentDistribution(value: String?) = when (value) {
    "center" -> ComponentDistribution.CENTER
    "equal" -> ComponentDistribution.EQUAL
    else -> ComponentDistribution.CENTER
}
