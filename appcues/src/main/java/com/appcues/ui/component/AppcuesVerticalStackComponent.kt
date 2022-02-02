package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.resolveDefaults
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getHorizontalAlignment

@Composable
internal fun VerticalStackComponent.Compose() {
    Column(
        modifier = Modifier.componentStyle(style, isSystemInDarkTheme()),
        horizontalAlignment = style.getHorizontalAlignment()
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
