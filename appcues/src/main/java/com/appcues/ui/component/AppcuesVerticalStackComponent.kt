package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.ExperienceComponent.VerticalStackComponent
import com.appcues.ui.extensions.ComposeEach
import com.appcues.ui.extensions.componentStyle
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.getTextStyle

@Composable
internal fun VerticalStackComponent.Compose() {
    Column(
        modifier = Modifier.componentStyle(style, isSystemInDarkTheme()),
        horizontalAlignment = style.getHorizontalAlignment(),
        verticalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {

        ProvideTextStyle(style.getTextStyle(LocalContext.current, isSystemInDarkTheme())) {
            items.ComposeEach()
        }
    }
}
