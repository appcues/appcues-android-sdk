package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.mapper.styling.mapComponentStyle
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextSpanResponse
import com.appcues.data.remote.response.styling.StyleResponse

internal fun TextPrimitiveResponse.mapTextPrimitive(): TextPrimitive {

    val processedSpans = when {
        // when text is not null and spans is null or empty we use the raw text as a single span item
        text != null && spans.isNullOrEmpty() -> text.toTextSpanPrimitiveList(style)
        // else when span is not null we nao to Primitive
        spans != null -> spans.toTextSpanPrimitive()
        // else an empty list
        else -> arrayListOf()
    }

    // check if this TextPrimitive is valid (contains text in form of spans)
    if (processedSpans.isEmpty()) {
        throw AppcuesMappingException("text($id) has no text or spans defined.")
    }

    return TextPrimitive(
        id = id,
        style = style.mapComponentStyle(),
        text = processedSpans.joinToString { it.text },
        spans = processedSpans
    )
}

private fun String.toTextSpanPrimitiveList(style: StyleResponse?): List<TextSpanPrimitive> {
    return listOf(TextSpanPrimitive(text = this, style = style.mapComponentStyle()))
}

private fun List<TextSpanResponse>.toTextSpanPrimitive(): List<TextSpanPrimitive> {
    return map { TextSpanPrimitive(text = it.text, style = it.style.mapComponentStyle()) }
}
