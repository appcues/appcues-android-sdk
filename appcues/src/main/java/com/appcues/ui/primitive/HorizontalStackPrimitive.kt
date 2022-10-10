package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.extensions.getVerticalAlignment
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import com.appcues.ui.utils.AppcuesArrangement
import com.appcues.util.eq
import java.util.UUID

@Composable
internal fun HorizontalStackPrimitive.Compose(modifier: Modifier) {

    val verticalAlignment = style.getVerticalAlignment(CenterVertically)
    Row(
        modifier = modifier,
        horizontalArrangement = distribution.toHorizontalArrangement(spacing),
        verticalAlignment = verticalAlignment
    ) {
        CompositionLocalProvider(LocalStackScope provides StackScope.ROW) {
            ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
                items.forEach {
                    ItemBox(distribution = distribution, primitive = it, parentVerticalAlignment = verticalAlignment) {
                        it.Compose()
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ItemBox(
    distribution: ComponentDistribution,
    primitive: ExperiencePrimitive,
    parentVerticalAlignment: Alignment.Vertical,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .align(primitive.style.getVerticalAlignment(parentVerticalAlignment))
            .then(
                if (distribution == ComponentDistribution.EQUAL ||
                    // for when its a spacer with no set spacing, it will fill available space
                    (primitive is SpacerPrimitive && primitive.spacing eq 0.0)
                ) Modifier.weight(1f)
                else Modifier
            ),
        contentAlignment = primitive.style.getBoxAlignment(),
        content = content
    )
}

private fun ComponentDistribution.toHorizontalArrangement(spacing: Double = 0.0): Arrangement.Horizontal {
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
            spacing = 10.0,
            style = ComponentStyle(
                width = 400.0,
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
            spacing = 8.0,
            style = ComponentStyle(
                width = 400.0,
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
            spacing = 20.0,
            style = ComponentStyle(
                width = 500.0,
                marginTop = 5.0,
                marginLeading = 4.0,
                marginBottom = 6.0,
                marginTrailing = 3.0,
                paddingTop = 6.0,
                paddingLeading = 7.0,
                paddingBottom = 9.0,
                paddingTrailing = 10.0,
                backgroundColor = ComponentColor(light = 0xFFCDCDFA, dark = 0xFFCDCDFA)
            ),
            items = items
        )
    }
}
