package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.toAnnotatedString

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f

@Composable
internal fun TextPrimitive.Compose(modifier: Modifier) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val style = LocalTextStyle.current.applyStyle(style, context, isDark)

    var resizedStyle by remember(id) { mutableStateOf(style) }
    var readyToDraw by remember(id) { mutableStateOf(false) }

    // skipping first compose ensures that when Text is added back to the Composition Nodes
    // it will measure/layout and create new instances that would otherwise be reused.
    var skipFirstCompose by remember(id) { mutableStateOf(true) }

    LaunchedEffect(key1 = skipFirstCompose) {
        skipFirstCompose = false
    }

    if (skipFirstCompose) return

    Text(
        modifier = modifier
            .clipToBounds()
            .drawWithContent { if (readyToDraw) drawContent() },
        text = spans.toAnnotatedString(context, isDark),
        style = resizedStyle,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
            // this will attempt to auto scale down single line / char text, handling cases
            // like emoji that are sized too large to fit in side by side layouts
            if (textLayoutResult.lineCount == 1) {
                if (textLayoutResult.isLineEllipsized(0)) {
                    resizedStyle = resizedStyle.copy(fontSize = resizedStyle.fontSize * TEXT_SCALE_REDUCTION_INTERVAL)
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        },
    )
}
