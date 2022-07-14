package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.styling.StyleMapper
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.remote.response.step.StepContentResponse

internal class BoxPrimitiveMapper(
    private val styleMapper: StyleMapper = StyleMapper(),
) {

    fun map(
        from: StepContentResponse,
        blockTransform: (StepContentResponse) -> ExperiencePrimitive
    ): ExperiencePrimitive {
        return from.mapBox(blockTransform)
    }

    private fun StepContentResponse.mapBox(
        stackItemsMapper: (StepContentResponse) -> ExperiencePrimitive,
    ) = BoxPrimitive(
        id = id,
        items = items?.map { stackItemsMapper(it) } ?: arrayListOf(),
        style = styleMapper.map(style),
    )
}
