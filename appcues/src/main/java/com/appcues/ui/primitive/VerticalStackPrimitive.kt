package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment.LEADING
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope.ColumnStackScope
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

@Composable
internal fun VerticalStackPrimitive.Compose(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = style.getHorizontalAlignment(),
        verticalArrangement = Arrangement.spacedBy(spacing.dp, Alignment.CenterVertically)
    ) {
        CompositionLocalProvider(LocalStackScope provides ColumnStackScope(style.width, items.size)) {
            ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
                items.forEach {
                    it.Compose()
                }
            }
        }
    }
}

private val items = arrayListOf(
    TextPrimitive(
        id = UUID.randomUUID(),
        text = "\uD83D\uDC4B Welcome!",
    ),
    ButtonPrimitive(
        id = UUID.randomUUID(),
        content = TextPrimitive(
            id = UUID.randomUUID(),
            text = "Button 1",
            style = ComponentStyle(
                fontSize = 17.0,
                foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF)
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
    )
)

@Composable
@Preview(name = "stack-Vertical-alignment.json", group = "extra")
internal fun PreviewTestVerticalAlignment() {
    AppcuesPreviewPrimitive {
        VerticalStackPrimitive(
            id = UUID.randomUUID(),
            style = ComponentStyle(
                horizontalAlignment = TRAILING,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-Vertical-default.json", group = "extra")
internal fun PreviewTestVerticalDefault() {
    AppcuesPreviewPrimitive {
        VerticalStackPrimitive(
            id = UUID.randomUUID(),
            style = ComponentStyle(
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-Vertical-layout.json", group = "extra")
internal fun PreviewTestVerticalLayout() {
    AppcuesPreviewPrimitive {
        VerticalStackPrimitive(
            id = UUID.randomUUID(),
            spacing = 20.0,
            style = ComponentStyle(
                marginTop = 5.0,
                marginLeading = 4.0,
                marginBottom = 6.0,
                marginTrailing = 3.0,
                paddingTop = 6.0,
                paddingLeading = 7.0,
                paddingBottom = 9.0,
                paddingTrailing = 10.0,
                horizontalAlignment = LEADING,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}
