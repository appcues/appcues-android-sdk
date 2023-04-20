package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned

/**
 * used to properly align step decorating trait content
 *
 * the main reason why we have this function is so that we do some internal calculations
 * to decide the amount of padding the main content will apply based on all step decorating traits
 * applied for that step
 */
internal fun Modifier.alignStepOverlay(
    boxScope: BoxScope,
    alignment: Alignment,
    stickyContentPadding: StickyContentPadding
): Modifier {
    return with(boxScope) {
        then(
            Modifier
                .align(alignment)
                .onGloballyPositioned {
                    if (alignment is BiasAlignment) {
                        with(alignment) {
                            when {
                                horizontalBias == -1f && verticalBias == 0f -> stickyContentPadding.setStartPadding(it.size.width)
                                horizontalBias == 1f && verticalBias == 0f -> stickyContentPadding.setEndPadding(it.size.width)
                                verticalBias == -1f -> stickyContentPadding.setTopPadding(it.size.height)
                                verticalBias == 1f -> stickyContentPadding.setBottomPadding(it.size.height)
                            }
                        }
                    }
                }
        )
    }
}
