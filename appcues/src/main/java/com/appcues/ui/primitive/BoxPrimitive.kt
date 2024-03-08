package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.ExperiencePrimitive.BoxPrimitive
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle

@Composable
internal fun BoxPrimitive.Compose(modifier: Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = style.getBoxAlignment(),
    ) {
        ProvideTextStyle(style.getTextStyle()) {
            items.forEach { it.Compose() }
        }
    }
}
