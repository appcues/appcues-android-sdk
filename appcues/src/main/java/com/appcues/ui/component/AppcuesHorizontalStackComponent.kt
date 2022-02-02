package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.resolveDefaults
import com.appcues.domain.entity.ExperienceComponent.HorizontalStackComponent
import com.appcues.domain.entity.styling.ComponentDistribution
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getVerticalAlignment

@Composable
internal fun HorizontalStackComponent.Compose() {
    Row(
        modifier = Modifier.componentStyle(style, isSystemInDarkTheme()),
        horizontalArrangement = distribution.toHorizontalArrangement(),
        verticalAlignment = style.getVerticalAlignment()
    ) {

        CompositionLocalProvider(
            LocalTextStyle provides resolveDefaults(LocalTextStyle.current, LocalLayoutDirection.current).applyStyle(
                style = style,
                context = LocalContext.current,
                isDark = isSystemInDarkTheme(),
            )
        ) {
            items.ComposeEach()
        }
    }
}

private fun ComponentDistribution.toHorizontalArrangement(): Arrangement.Horizontal {
    return when (this) {
        ComponentDistribution.CENTER -> Arrangement.Center
        ComponentDistribution.EQUAL -> Arrangement.SpaceAround
    }
}
