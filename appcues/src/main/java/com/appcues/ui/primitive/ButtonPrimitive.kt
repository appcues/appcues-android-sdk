package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentShadow
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

@Composable
internal fun ButtonPrimitive.Compose(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            Box(
                contentAlignment = content.style.getBoxAlignment(),
                modifier = Modifier.styleButtonContentWidth(style),
            ) {
                content.Compose()
            }
        }
    }
}

private fun Modifier.styleButtonContentWidth(buttonStyle: ComponentStyle) =
    if (buttonStyle.width != null) {
        this.fillMaxWidth()
    } else {
        Modifier
    }

@Composable
@Preview(name = "button-border.json", group = "extra")
internal fun PreviewButtonBorder() {
    AppcuesPreviewPrimitive {
        ButtonPrimitive(
            id = UUID.randomUUID(),
            content = TextPrimitive(
                id = UUID.randomUUID(),
                style = ComponentStyle(
                    foregroundColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
                ),
                spans = arrayListOf(
                    TextSpanPrimitive(
                        text = "My Fancy Button",
                        style = ComponentStyle(
                            foregroundColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
                        )
                    )
                )
            ),
            style = ComponentStyle(
                paddingTop = 8.0,
                paddingLeading = 18.0,
                paddingBottom = 8.0,
                paddingTrailing = 18.0,
                marginTop = 1.0,
                marginLeading = 1.0,
                marginBottom = 1.0,
                marginTrailing = 1.0,
                cornerRadius = 10.0,
                borderWidth = 1.0,
                borderColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
            )
        )
    }
}

@Composable
@Preview(name = "button-complex-contents.json", group = "extra")
internal fun PreviewButtonComplexContents() {
    AppcuesPreviewPrimitive {
        ButtonPrimitive(
            id = UUID.randomUUID(),
            content = HorizontalStackPrimitive(
                id = UUID.randomUUID(),
                spacing = 8.0,
                items = arrayListOf(
                    TextPrimitive(
                        id = UUID.randomUUID(),
                        spans = arrayListOf(
                            TextSpanPrimitive("\uD83E\uDDEA")
                        )
                    ),
                    TextPrimitive(
                        id = UUID.randomUUID(),
                        spans = arrayListOf(
                            TextSpanPrimitive("My Fancy Button")
                        )
                    )
                )
            ),
            style = ComponentStyle(
                paddingTop = 8.0,
                paddingLeading = 8.0,
                paddingBottom = 8.0,
                paddingTrailing = 8.0,
                cornerRadius = 2.0,
                foregroundColor = ComponentColor(light = 0xFF0B1A38, dark = 0xFF0B1A38),
                backgroundColor = ComponentColor(light = 0xFF20E0D6, dark = 0xFF20E0D6),
            )

        )
    }
}

@Composable
@Preview(name = "button-default.json", group = "extra")
internal fun PreviewButtonDefault() {
    AppcuesPreviewPrimitive {
        ButtonPrimitive(
            id = UUID.randomUUID(),
            content = TextPrimitive(
                id = UUID.randomUUID(),
                spans = arrayListOf(
                    TextSpanPrimitive("My Button")
                )
            ),
        )
    }
}

@Composable
@Preview(name = "button-general.json", group = "extra")
internal fun PreviewButtonGeneral() {
    AppcuesPreviewPrimitive {
        ButtonPrimitive(
            id = UUID.randomUUID(),
            content = TextPrimitive(
                id = UUID.randomUUID(),
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
                marginTop = 10.0,
                marginTrailing = 10.0,
                marginBottom = 10.0,
                marginLeading = 10.0,
                paddingTop = 8.0,
                paddingLeading = 18.0,
                paddingBottom = 8.0,
                paddingTrailing = 18.0,
                backgroundGradient = arrayListOf(
                    ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF),
                    ComponentColor(light = 0xFF8960FF, dark = 0xFF8960FF)
                ),
                cornerRadius = 6.0,
                shadow = ComponentShadow(
                    color = ComponentColor(light = 0xFF778899, dark = 0xFF778899),
                    radius = 6.0,
                    x = 2.0,
                    y = 2.0,
                )
            )
        )
    }
}
