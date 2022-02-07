package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getTextStyle
import com.appcues.ui.extensions.getVerticalAlignment

@Composable
internal fun HorizontalStackComponent.Compose() {
    Row(
        modifier = Modifier.componentStyle(style, isSystemInDarkTheme()),
        horizontalArrangement = distribution.toHorizontalArrangement(spacing),
        verticalAlignment = style.getVerticalAlignment()
    ) {

        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            items.ComposeEach()
        }
    }
}

private fun ComponentDistribution.toHorizontalArrangement(spacing: Int = 0): Arrangement.Horizontal {
    if (spacing > 0) return Arrangement.spacedBy(spacing.dp)

    return when (this) {
        ComponentDistribution.CENTER -> Arrangement.Center
        ComponentDistribution.EQUAL -> Arrangement.SpaceAround
    }
}
