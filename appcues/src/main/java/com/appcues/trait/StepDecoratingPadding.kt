package com.appcues.trait

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Density

class StepDecoratingPadding(private val density: Density) {

    private val topPaddingPx = mutableStateOf(0)
    private val bottomPaddingPx = mutableStateOf(0)
    private val startPaddingPx = mutableStateOf(0)
    private val endPaddingPx = mutableStateOf(0)

    fun setTopPadding(px: Int) {
        if (topPaddingPx.value < px) {
            topPaddingPx.value = px
        }
    }

    fun setBottomPadding(px: Int) {
        if (bottomPaddingPx.value < px) {
            bottomPaddingPx.value = px
        }
    }

    fun setStartPadding(px: Int) {
        if (startPaddingPx.value < px) {
            startPaddingPx.value = px
        }
    }

    fun setEndPadding(px: Int) {
        if (endPaddingPx.value < px) {
            endPaddingPx.value = px
        }
    }

    val paddingValues = derivedStateOf {
        with(density) {
            PaddingValues(
                start = startPaddingPx.value.toDp(),
                top = topPaddingPx.value.toDp(),
                end = endPaddingPx.value.toDp(),
                bottom = bottomPaddingPx.value.toDp()
            )
        }
    }
}
