package com.appcues.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.ui.LocalAppcuesActionDelegate
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.Compose
import com.appcues.ui.extensions.PrimitiveGestureProperties
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.extensions.primitiveStyle
import com.appcues.ui.theme.AppcuesPreview
import java.util.UUID

@Composable
internal fun BoxPrimitive.Compose() {
    Box(
        modifier = Modifier.primitiveStyle(
            component = this,
            gestureProperties = PrimitiveGestureProperties(
                onAction = LocalAppcuesActionDelegate.current.onAction,
                actions = LocalAppcuesActions.current,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                enabled = remember { true },
            ),
            isDark = isSystemInDarkTheme()
        ),
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
    ),
    ButtonPrimitive(
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
            paddingTop = 8,
            paddingTrailing = 18,
            paddingBottom = 8,
            paddingLeading = 18,
            backgroundGradient = arrayListOf(
                ComponentColor(light = 0xFF5C5CFF, dark = 0xFF5C5CFF),
                ComponentColor(light = 0xFF8960FF, dark = 0xFF8960FF)
            ),
            cornerRadius = 6
        )
    ),
    TextPrimitive(
        id = UUID.randomUUID(),
        text = "BYE! \uD83E\uDD96",
    )
)

@Composable
@Preview
internal fun PreviewTestBoxDefault() {
    val component = BoxPrimitive(
        id = UUID.randomUUID(),
        items = items
    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview
internal fun PreviewTestBoxAlignment() {
    val component = BoxPrimitive(
        id = UUID.randomUUID(),
        style = ComponentStyle(
            backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA),
            verticalAlignment = ComponentVerticalAlignment.BOTTOM,
            horizontalAlignment = ComponentHorizontalAlignment.TRAILING,
        ),
        items = items
    )

    AppcuesPreview {
        component.Compose()
    }
}
