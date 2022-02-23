package com.appcues.ui.component

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentShadow
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.Compose
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.theme.AppcuesPreview
import java.util.UUID

@Composable
internal fun ButtonPrimitive.Compose() {
    val onAction = LocalAppcuesActions.current.onAction
    val interactionSource = remember { MutableInteractionSource() }
    val isEnabled = remember { true }

    Surface(
        modifier = Modifier
            .componentStyle(
                style = style,
                isDark = isSystemInDarkTheme(),
                clickModifier = Modifier.clickableButton(
                    interactionSource = interactionSource,
                    indication = rememberRipple(),
                    enabled = isEnabled,
                    onClick = { actions.forEach { onAction(it.experienceAction) } }
                )
            ),
        color = Color.Transparent,
    ) {
        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                content.Compose()
            }
        }
    }
}

private fun Modifier.clickableButton(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClick: () -> Unit
) = this.then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled,
        onClickLabel = null,
        role = Role.Button,
        onClick = onClick,
    )
)

@Composable
@Preview(name = "button-border.json", group = "extra")
internal fun PreviewButtonBorder() {
    val component = ButtonPrimitive(
        id = UUID.randomUUID(),
        content = TextPrimitive(
            id = UUID.randomUUID(),
            text = "My Fancy Button",
            style = ComponentStyle(
                foregroundColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
            )
        ),
        style = ComponentStyle(
            paddingTop = 8,
            paddingLeading = 18,
            paddingBottom = 8,
            paddingTrailing = 18,
            marginTop = 1,
            marginLeading = 1,
            marginBottom = 1,
            marginTrailing = 1,
            cornerRadius = 10,
            borderWidth = 1,
            borderColor = ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF)
        )
    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview(name = "button-complex-contents.json", group = "extra")
internal fun PreviewButtonComplexContents() {
    val component = ButtonPrimitive(
        id = UUID.randomUUID(),
        content = HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            spacing = 8,
            items = arrayListOf(
                TextPrimitive(
                    id = UUID.randomUUID(),
                    text = "\uD83E\uDDEA"
                ),
                TextPrimitive(
                    id = UUID.randomUUID(),
                    text = "My Fancy Button"
                )
            )
        ),
        style = ComponentStyle(
            paddingTop = 8,
            paddingLeading = 8,
            paddingBottom = 8,
            paddingTrailing = 8,
            cornerRadius = 2,
            foregroundColor = ComponentColor(light = 0xFF0B1A38, dark = 0xFF0B1A38),
            backgroundColor = ComponentColor(light = 0xFF20E0D6, dark = 0xFF20E0D6),
        )

    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview(name = "button-default.json", group = "extra")
internal fun PreviewButtonDefault() {
    val component = ButtonPrimitive(
        id = UUID.randomUUID(),
        content = TextPrimitive(
            id = UUID.randomUUID(),
            text = "My Button",
        ),
    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview(name = "button-general.json", group = "extra")
internal fun PreviewButtonGeneral() {
    val component = ButtonPrimitive(
        id = UUID.randomUUID(),
        content = TextPrimitive(
            id = UUID.randomUUID(),
            text = "Button 1",
            style = ComponentStyle(
                fontSize = 17,
                foregroundColor = ComponentColor(light = 0xFFFFFFFF, dark = 0xFFFFFFFF)
            )
        ),
        style = ComponentStyle(
            marginTop = 10,
            marginTrailing = 10,
            marginBottom = 10,
            marginLeading = 10,
            paddingTop = 8,
            paddingLeading = 18,
            paddingBottom = 8,
            paddingTrailing = 18,
            backgroundGradient = arrayListOf(
                ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF),
                ComponentColor(light = 0xFF8960FF, dark = 0xFF8960FF)
            ),
            cornerRadius = 6,
            shadow = ComponentShadow(
                color = ComponentColor(light = 0xFF778899, dark = 0xFF778899),
                radius = 6,
                x = 2,
                y = 2,
            )
        )
    )

    AppcuesPreview {
        component.Compose()
    }
}
