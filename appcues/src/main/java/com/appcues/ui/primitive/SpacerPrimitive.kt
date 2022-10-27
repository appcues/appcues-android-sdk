package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive
import com.appcues.ui.composables.LocalStackScope
import com.appcues.ui.composables.StackScope.ColumnStackScope
import com.appcues.ui.composables.StackScope.RowStackScope

@Composable
internal fun SpacerPrimitive.Compose(modifier: Modifier) {
    // use spacing to set width or height based on LocalStackScope
    if (spacing > 0) {
        val sizeModifier = when (LocalStackScope.current) {
            is ColumnStackScope -> Modifier.height(spacing.dp)
            is RowStackScope -> Modifier.width(spacing.dp)
        }

        Spacer(
            modifier = modifier
                .then(sizeModifier)
        )
    }
}
