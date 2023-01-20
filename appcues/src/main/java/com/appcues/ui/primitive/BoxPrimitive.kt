package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

@Composable
internal fun BoxPrimitive.Compose(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = style.getBoxAlignment(),
    ) {
        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            items.forEach { it.Compose() }
        }
    }
}

private val items = arrayListOf(
    TextPrimitive(
        id = UUID.randomUUID(),
        text = "\uD83D\uDC4B Welcome!",
        spans = arrayListOf(TextSpanPrimitive("\uD83D\uDC4B Welcome!"))
    ),
    ButtonPrimitive(
        id = UUID.randomUUID(),
        content = TextPrimitive(
            id = UUID.randomUUID(),
            text = "Button 1",
            style = ComponentStyle(
                fontSize = 17.0,
                foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF)
            ),
            spans = arrayListOf(
                TextSpanPrimitive(
                    text = "Button 1",
                    style = ComponentStyle(
                        fontSize = 17.0,
                        foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF)
                    )
                )
            )
        ),
        style = ComponentStyle(
            paddingTop = 8.0,
            paddingTrailing = 18.0,
            paddingBottom = 8.0,
            paddingLeading = 18.0,
            backgroundGradient = arrayListOf(
                ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF),
                ComponentColor(light = 0xFF8960FF, dark = 0xFF8960FF)
            ),
            cornerRadius = 6.0
        )
    ),
    TextPrimitive(
        id = UUID.randomUUID(),
        text = "BYE! \uD83E\uDD96",
        spans = arrayListOf(TextSpanPrimitive("BYE! \uD83E\uDD96"))
    )
)

@Composable
@Preview
internal fun PreviewTestBoxDefault() {
    AppcuesPreviewPrimitive {
        BoxPrimitive(
            id = UUID.randomUUID(),
            items = items
        )
    }
}

@Composable
@Preview
internal fun PreviewTestBoxAlignment() {
    AppcuesPreviewPrimitive {
        BoxPrimitive(
            id = UUID.randomUUID(),
            style = ComponentStyle(
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA),
                verticalAlignment = ComponentVerticalAlignment.BOTTOM,
                horizontalAlignment = ComponentHorizontalAlignment.TRAILING,
            ),
            items = items
        )
    }
}
