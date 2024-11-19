package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.HorizontalStackPrimitive
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.data.model.styling.ComponentDistribution
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope
import com.appcues.ui.extensions.conditional
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.extensions.getVerticalAlignment
import com.appcues.ui.utils.AppcuesArrangement
import com.appcues.util.eq

@Composable
internal fun HorizontalStackPrimitive.Compose(modifier: Modifier) {
    val verticalAlignment = style.getVerticalAlignment(CenterVertically)
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = distribution.toHorizontalArrangement(spacing),
        verticalAlignment = verticalAlignment
    ) {
        CompositionLocalProvider(LocalStackScope provides StackScope.ROW) {
            ProvideTextStyle(style.getTextStyle()) {
                items.forEach {
                    ItemBox(
                        primitive = it,
                        distribution = distribution,
                        parentVerticalAlignment = verticalAlignment
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ItemBox(
    primitive: ExperiencePrimitive,
    distribution: ComponentDistribution,
    parentVerticalAlignment: Alignment.Vertical,
) {
    Box(
        modifier = Modifier
            .align(primitive.style.getVerticalAlignment(parentVerticalAlignment))
            .conditional(
                distribution == ComponentDistribution.EQUAL ||
                    // for when its a spacer with no set spacing, it will fill available space
                    (primitive is SpacerPrimitive && primitive.spacing eq 0.0)
            ) { weight(1f) },
        contentAlignment = primitive.style.getBoxAlignment()
    ) {
        primitive.Compose()
    }
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
