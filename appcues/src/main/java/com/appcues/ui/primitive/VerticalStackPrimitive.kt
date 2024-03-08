package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appcues.data.model.ExperiencePrimitive.VerticalStackPrimitive
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.getTextStyle

@Composable
internal fun VerticalStackPrimitive.Compose(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = style.getHorizontalAlignment(Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(spacing.dp, Alignment.CenterVertically)
    ) {
        CompositionLocalProvider(LocalStackScope provides StackScope.COLUMN) {
            ProvideTextStyle(style.getTextStyle()) {
                items.forEach {
                    it.Compose()
                }
            }
        }
    }
}
