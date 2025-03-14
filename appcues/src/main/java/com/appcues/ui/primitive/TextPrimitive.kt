package com.appcues.ui.primitive

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.style.TextOverflow
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.toAnnotatedString

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f

@Composable
internal fun TextPrimitive.Compose(modifier: Modifier) {
    val textStyle = LocalTextStyle.current.applyStyle(style)
    val spansText = spans.joinToString(separator = String()) { it.text }

    var resizedStyle by remember(id, spansText, textStyle) { mutableStateOf(textStyle) }
    var resizedSpans by remember(id, spansText, textStyle) { mutableStateOf(spans) }
    var readyToDraw by remember(id, spansText, textStyle) { mutableStateOf(false) }

    key(resizedStyle) {

        Text(
            modifier = modifier
                .clipToBounds()
                .drawWithContent { if (readyToDraw) drawContent() },
            text = resizedSpans.toAnnotatedString(),
            style = resizedStyle,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                // this will attempt to auto scale down single line / char text, handling cases
                // like emoji that are sized too large to fit in side by side layouts
                if (textLayoutResult.lineCount == 1) {
                    if (textLayoutResult.isLineEllipsized(0)) {
                        resizedStyle = resizedStyle.copy(fontSize = resizedStyle.fontSize * TEXT_SCALE_REDUCTION_INTERVAL)
                        resizedSpans = resizedSpans.map { span ->
                            val fontSize = span.style.fontSize?.let { it * TEXT_SCALE_REDUCTION_INTERVAL }
                            span.copy(style = span.style.copy(fontSize = fontSize))
                        }
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            },
        )
    }
}
