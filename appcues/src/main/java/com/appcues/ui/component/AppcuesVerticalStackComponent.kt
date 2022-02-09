package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentStyle
import com.appcues.domain.entity.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.domain.entity.styling.ComponentStyle.ComponentHorizontalAlignment.TRAILING
import com.appcues.ui.extensions.Compose
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.theme.AppcuesPreview
import java.util.UUID

@Composable
internal fun VerticalStackComponent.Compose() {
    Column(
        modifier = Modifier.componentStyle(style, isSystemInDarkTheme()),
        horizontalAlignment = style.getHorizontalAlignment(),
        verticalArrangement = Arrangement.spacedBy(spacing.dp, Alignment.CenterVertically)
    ) {

        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            items.forEach { it.Compose() }
        }
    }
}

private val items = arrayListOf(
    TextComponent(
        id = UUID.randomUUID(),
        text = "\uD83D\uDC4B Welcome!",
    ),
    ButtonComponent(
        id = UUID.randomUUID(),
        content = TextComponent(
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
    TextComponent(
        id = UUID.randomUUID(),
        text = "BYE! \uD83E\uDD96",
    )
)

@Composable
@Preview(name = "stack-Vertical-alignment.json", group = "extra")
internal fun PreviewTestVerticalAlignment() {
    val component = VerticalStackComponent(
        id = UUID.randomUUID(),
        style = ComponentStyle(
            horizontalAlignment = TRAILING,
            backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
        ),
        items = items
    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview(name = "stack-Vertical-default.json", group = "extra")
internal fun PreviewTestVerticalDefault() {
    val component = VerticalStackComponent(
        id = UUID.randomUUID(),
        style = ComponentStyle(
            backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
        ),
        items = items
    )

    AppcuesPreview {
        component.Compose()
    }
}

@Composable
@Preview(name = "stack-Vertical-layout.json", group = "extra")
internal fun PreviewTestVerticalLayout() {
    val component = VerticalStackComponent(
        id = UUID.randomUUID(),
        spacing = 20,
        style = ComponentStyle(
            marginTop = 5,
            marginLeading = 4,
            marginBottom = 6,
            marginTrailing = 3,
            paddingTop = 6,
            paddingLeading = 7,
            paddingBottom = 9,
            paddingTrailing = 10,
            horizontalAlignment = ComponentHorizontalAlignment.LEADING,
            backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
        ),
        items = items
    )

    AppcuesPreview {
        component.Compose()
    }
}
