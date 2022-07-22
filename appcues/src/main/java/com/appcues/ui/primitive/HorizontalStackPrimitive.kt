package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.extensions.getVerticalAlignment
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import com.appcues.ui.utils.AppcuesArrangement
import java.util.UUID

@Composable
internal fun HorizontalStackPrimitive.Compose(modifier: Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = distribution.toHorizontalArrangement(spacing),
        verticalAlignment = style.getVerticalAlignment()
    ) {
        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            items.forEach {
                ItemBox(distribution = distribution, style = it.style) {
                    it.Compose()
                }
            }
        }
    }
}

@Composable
private fun RowScope.ItemBox(
    distribution: ComponentDistribution,
    style: ComponentStyle,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .align(style.getVerticalAlignment())
            .then(if (distribution == ComponentDistribution.EQUAL) Modifier.weight(1f) else Modifier),
        contentAlignment = style.getBoxAlignment(),
        content = content
    )
}

private fun ComponentDistribution.toHorizontalArrangement(spacing: Int = 0): Arrangement.Horizontal {
    return when (this) {
        ComponentDistribution.CENTER -> AppcuesArrangement.spacedCenter(spacing)
        ComponentDistribution.EQUAL -> AppcuesArrangement.spacedEvenly(spacing)
    }
}

private fun ComponentStyle.getBoxAlignment(): Alignment {
    return when (horizontalAlignment) {
        ComponentHorizontalAlignment.LEADING -> Alignment.CenterStart
        ComponentHorizontalAlignment.CENTER -> Alignment.Center
        ComponentHorizontalAlignment.TRAILING -> Alignment.CenterEnd
        else -> Alignment.Center
    }
}

private val items = arrayListOf(
    TextPrimitive(
        id = UUID.randomUUID(),
        text = "\uD83D\uDC4B Welcome!",
        style = ComponentStyle(
            verticalAlignment = ComponentVerticalAlignment.BOTTOM,
            horizontalAlignment = ComponentHorizontalAlignment.LEADING,
        )
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
        style = ComponentStyle(
            verticalAlignment = ComponentVerticalAlignment.TOP,
            horizontalAlignment = ComponentHorizontalAlignment.TRAILING,
        )
    )
)

@Composable
@Preview(name = "stack-horizontal-alignment.json", group = "extra")
internal fun PreviewTestHorizontalAlignment() {
    AppcuesPreviewPrimitive {
        HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            style = ComponentStyle(
                verticalAlignment = ComponentVerticalAlignment.TOP,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-horizontal-default.json", group = "extra")
internal fun PreviewTestHorizontalDefault() {
    AppcuesPreviewPrimitive {
        HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            style = ComponentStyle(
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-horizontal-distribution-equal.json", group = "extra")
internal fun PreviewTestHorizontalDistributionEqual() {
    AppcuesPreviewPrimitive {
        HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            distribution = ComponentDistribution.EQUAL,
            spacing = 10,
            style = ComponentStyle(
                width = 400,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-horizontal-distribution-center.json", group = "extra")
internal fun PreviewTestHorizontalDistributionCenter() {
    AppcuesPreviewPrimitive {
        HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            distribution = ComponentDistribution.CENTER,
            spacing = 8,
            style = ComponentStyle(
                width = 400,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}

@Composable
@Preview(name = "stack-horizontal-layout.json", group = "extra")
internal fun PreviewTestHorizontalLayout() {
    AppcuesPreviewPrimitive {
        HorizontalStackPrimitive(
            id = UUID.randomUUID(),
            spacing = 20,
            style = ComponentStyle(
                width = 500,
                marginTop = 5,
                marginLeading = 4,
                marginBottom = 6,
                marginTrailing = 3,
                paddingTop = 6,
                paddingLeading = 7,
                paddingBottom = 9,
                paddingTrailing = 10,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}
