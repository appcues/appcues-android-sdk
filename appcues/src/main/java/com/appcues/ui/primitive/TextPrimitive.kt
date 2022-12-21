package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.composables.LocalCompositionTranslation
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

private const val TEXT_SCALE_REDUCTION_INTERVAL = 0.9f

@Composable
internal fun TextPrimitive.Compose(modifier: Modifier) {
    val style = LocalTextStyle.current.applyStyle(
        style = style,
        context = LocalContext.current,
        isDark = isSystemInDarkTheme(),
    )

    var resizedStyle by remember(style) { mutableStateOf(style) }
    var readyToDraw by remember(style) { mutableStateOf(false) }

    val translation = LocalCompositionTranslation.current
    val textToRender = remember { mutableStateOf(text) }

    LaunchedEffect(translation?.translateComposition?.value) {
        translation?.let {
            textToRender.value = if (it.translateComposition.value) {
                 it.translateBlock(text, it.sourceLanguageTag, it.targetLanguageTag)
            } else {
                text
            }
        }
    }

    Text(
        modifier = modifier.clipToBounds().drawWithContent { if (readyToDraw) drawContent() },
        text = textToRender.value,
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

private val textPrimitive = TextPrimitive(
    id = UUID.randomUUID(),
    text = "\uD83D\uDC4B Welcome! If you’re looking for Appcues’\nbrand guidelines, you’ve come to the right place.",
)

@Composable
@Preview(name = "Text Color", group = "properties")
internal fun PreviewTextComponentColor() {
    val previewPrimitive = textPrimitive.copy(
        style = textPrimitive.style.copy(
            foregroundColor = ComponentColor(light = 0xFF000000, dark = 0xFFFFFFFF)
        )
    )

    Column {
        AppcuesPreviewPrimitive(isDark = true) {
            previewPrimitive
        }
        AppcuesPreviewPrimitive(isDark = false) {
            previewPrimitive
        }
    }
}

@Composable
@Preview(name = "lineHeight", group = "properties")
internal fun PreviewTextComponentLineHeight() {
    Column {
        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(lineHeight = 30.0))
        }
        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(lineHeight = 40.0))
        }
    }
}

@Composable
@Preview(name = "letterSpacing", group = "properties")
internal fun PreviewTextComponentLetterSpacing() {
    Column {
        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(letterSpacing = 5.0))
        }

        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(letterSpacing = 10.0))
        }
    }
}

@Composable
@Preview(name = "text-default.json", group = "extra")
internal fun PreviewTestDefault() {
    AppcuesPreviewPrimitive {
        textPrimitive
    }
}

@Composable
@Preview(name = "text-border.json", group = "extra")
internal fun PreviewTestBorder() {
    AppcuesPreviewPrimitive {
        textPrimitive.copy(
            style = textPrimitive.style.copy(
                paddingTop = 5.0,
                paddingLeading = 5.0,
                paddingBottom = 5.0,
                paddingTrailing = 5.0,
                marginTop = 2.0,
                marginTrailing = 2.0,
                marginBottom = 2.0,
                marginLeading = 2.0,
                cornerRadius = 10.0,
                borderWidth = 4.0,
                borderColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
            )
        )
    }
}

@Composable
@Preview(name = "text-fixed-size.json / FAILING", group = "extra")
internal fun PreviewTestFixedSize() {
    AppcuesPreviewPrimitive {
        textPrimitive.copy(
            style = textPrimitive.style.copy(
                width = 200.0,
                height = 24.0,
                foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF),
                backgroundColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
            )
        )
    }
}

@Composable
@Preview(name = "text-layout.json", group = "extra")
internal fun PreviewTestLayout() {
    AppcuesPreviewPrimitive {
        textPrimitive.copy(
            style = textPrimitive.style.copy(
                marginTop = 5.0,
                marginLeading = 4.0,
                marginBottom = 6.0,
                marginTrailing = 3.0,
                paddingTop = 6.0,
                paddingLeading = 7.0,
                paddingBottom = 9.0,
                paddingTrailing = 10.0,
                foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF),
                backgroundColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
            )
        )
    }
}

@Composable
@Preview(name = "text-system-font.json", group = "extra")
internal fun PreviewTestSystemFont() {
    AppcuesPreviewPrimitive {
        textPrimitive.copy(
            style = textPrimitive.style.copy(
                width = 180.0,
                fontSize = 15.0,
                foregroundColor = ComponentColor(light = 0xFFF5F7FA, dark = 0xFFF5F7FA),
                backgroundColor = ComponentColor(light = 0xFFFF5290, dark = 0xFFFF5290)
            )
        )
    }
}

@Composable
@Preview(name = "text-typography.json", group = "extra")
internal fun PreviewTestTypography() {
    AppcuesPreviewPrimitive {
        textPrimitive.copy(
            style = textPrimitive.style.copy(
                paddingTop = 8.0,
                paddingLeading = 8.0,
                paddingBottom = 8.0,
                paddingTrailing = 2.0,
                fontSize = 12.0,
                letterSpacing = 6.0,
                lineHeight = 30.0,
                textAlignment = TRAILING,
                foregroundColor = ComponentColor(light = 0xFF0B1A38, dark = 0xFF0B1A38),
            )
        )
    }
}
