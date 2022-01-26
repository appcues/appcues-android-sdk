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

internal fun ComponentStyle.contentPadding() = PaddingValues(
    start = paddingLeading.dp,
    top = paddingTop.dp,
    bottom = paddingBottom.dp,
    end = paddingTrailing.dp,
)
