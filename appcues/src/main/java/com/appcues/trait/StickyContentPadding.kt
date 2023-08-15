package com.appcues.trait

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Density

/**
 * Type that defines the padding values necessary to account for sticky content elements when composing the step.
 */
internal class StickyContentPadding(private val density: Density) {

    private val topPaddingPx = mutableStateOf(0)
    private val bottomPaddingPx = mutableStateOf(0)
    private val startPaddingPx = mutableStateOf(0)
    private val endPaddingPx = mutableStateOf(0)

    /**
     * Update the top padding for sticky content, if value is larger
     * than existing value.
     *
     * @param px the updated top padding value, in pixels.
     */
    fun setTopPadding(px: Int) {
        if (topPaddingPx.value < px) {
            topPaddingPx.value = px
        }
    }

    /**
     * Update the bottom padding for sticky content, if value is larger
     * than existing value.
     *
     * @param px the updated bottom padding value, in pixels.
     */
    fun setBottomPadding(px: Int) {
        if (bottomPaddingPx.value < px) {
            bottomPaddingPx.value = px
        }
    }

    /**
     * Update the start padding for sticky content, if value is larger
     * than existing value.
     *
     * @param px the updated start padding value, in pixels.
     */
    fun setStartPadding(px: Int) {
        if (startPaddingPx.value < px) {
            startPaddingPx.value = px
        }
    }

    /**
     * Update the end padding for sticky content, if value is larger
     * than existing value.
     *
     * @param px the updated end padding value, in pixels.
     */
    fun setEndPadding(px: Int) {
        if (endPaddingPx.value < px) {
            endPaddingPx.value = px
        }
    }

    /**
     * The PaddingValues that represent the current state of this
     * StickyContentPadding instance, converted to Dp.
     */
    val paddingValues: State<PaddingValues> = derivedStateOf {
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
