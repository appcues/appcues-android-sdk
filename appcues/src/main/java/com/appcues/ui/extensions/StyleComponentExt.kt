package com.appcues.ui.extensions

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.styling.ComponentStyle

internal fun ComponentStyle.padding() = PaddingValues(
    start = marginLeading.dp,
    top = marginTop.dp,
    bottom = marginBottom.dp,
    end = marginTrailing.dp,
)
