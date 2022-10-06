package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.ExperiencePrimitive.SpacerPrimitive

@Composable
internal fun SpacerPrimitive.Compose(modifier: Modifier) {
    Spacer(modifier = modifier)
}
