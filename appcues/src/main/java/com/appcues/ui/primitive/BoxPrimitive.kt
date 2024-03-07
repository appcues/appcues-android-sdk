package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.ui.composables.LocalPackageNames
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle

@Composable
internal fun BoxPrimitive.Compose(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = style.getBoxAlignment(),
    ) {
        ProvideTextStyle(style.getTextStyle(LocalContext.current, LocalPackageNames.current, isSystemInDarkTheme())) {
            items.forEach { it.Compose() }
        }
    }
}
