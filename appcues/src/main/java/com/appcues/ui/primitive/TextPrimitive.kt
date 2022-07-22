package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

@Composable
internal fun TextPrimitive.Compose(modifier: Modifier) {
    Text(
        modifier = modifier,
        text = text,
        style = LocalTextStyle.current.applyStyle(
            style = style,
            context = LocalContext.current,
            isDark = isSystemInDarkTheme(),
        ),
        overflow = TextOverflow.Ellipsis, // Not Working https://issuetracker.google.com/issues/168720622
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
            textPrimitive.copy(style = textPrimitive.style.copy(lineHeight = 30))
        }
        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(lineHeight = 40))
        }
    }
}

@Composable
@Preview(name = "letterSpacing", group = "properties")
internal fun PreviewTextComponentLetterSpacing() {
    Column {
        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(letterSpacing = 5))
        }

        AppcuesPreviewPrimitive {
            textPrimitive.copy(style = textPrimitive.style.copy(letterSpacing = 10))
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
                paddingTop = 5,
                paddingLeading = 5,
                paddingBottom = 5,
                paddingTrailing = 5,
                marginTop = 2,
                marginTrailing = 2,
                marginBottom = 2,
                marginLeading = 2,
                cornerRadius = 10,
                borderWidth = 4,
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
                width = 200,
                height = 24,
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
                marginTop = 5,
                marginLeading = 4,
                marginBottom = 6,
                marginTrailing = 3,
                paddingTop = 6,
                paddingLeading = 7,
                paddingBottom = 9,
                paddingTrailing = 10,
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
                width = 180,
                fontSize = 15,
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
                paddingTop = 8,
                paddingLeading = 8,
                paddingBottom = 8,
                paddingTrailing = 2,
                fontSize = 12,
                letterSpacing = 6,
                lineHeight = 30,
                textAlignment = TRAILING,
                foregroundColor = ComponentColor(light = 0xFF0B1A38, dark = 0xFF0B1A38),
            )
        )
    }
}
