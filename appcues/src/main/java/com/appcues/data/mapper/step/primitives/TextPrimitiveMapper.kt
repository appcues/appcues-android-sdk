package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextSpanResponse
import com.appcues.data.remote.appcues.response.styling.StyleResponse

internal fun TextPrimitiveResponse.mapTextPrimitive(): TextPrimitive {
    return TextPrimitive(
        id = id,
        style = style.mapComponentStyle(),
        spans = when {
            spans != null -> spans.toTextSpanPrimitive()
            text != null -> text.toTextSpanPrimitiveList(style)
            else -> throw AppcuesMappingException("text($id) has no text or spans defined.")
        }
    )
}

private fun String.toTextSpanPrimitiveList(style: StyleResponse?): List<TextSpanPrimitive> {
    return listOf(TextSpanPrimitive(text = this, style = style.mapComponentStyle()))
}

private fun List<TextSpanResponse>.toTextSpanPrimitive(): List<TextSpanPrimitive> {
    return map { TextSpanPrimitive(text = it.text, style = it.style.mapComponentStyle()) }
}
