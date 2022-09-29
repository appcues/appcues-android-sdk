package com.appcues.ui.composables

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.appcues.ui.AppcuesViewModel.UIState
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.util.getNavigationBarHeight
import com.appcues.util.getStatusBarHeight

@Composable
internal fun rememberLastRenderingState(state: State<UIState>) = remember { mutableStateOf<Rendering?>(null) }
    .apply {
        value = state.value.let { uiState ->
            if (uiState is Rendering) {
                // if UIState is rendering then we set new value and show content
                isContentVisible.targetState = true
                uiState
            } else {
                // else we keep the same value and hide content to trigger dismissing animation
                isContentVisible.targetState = false
                value
            }
        }
    }

/**
 * AppcuesPaginationData is used to communicate between different traits
 * information regarding pagination.
 *
 * Every [ContentHolderTrait] should update this information if other
 * traits are supposed to react to stuff like page changes, horizontal scrolling
 * between pages, etc..
 */
data class AppcuesPaginationData(
    val pageCount: Int,
    val currentPage: Int,
    val scrollOffset: Float
)

internal val appcuesPaginationData = mutableStateOf(AppcuesPaginationData(1, 0, 0.0f))

/**
 * rememberAppcuesPaginationState is used by traits that wants to know updates about pagination data
 * returns a State of AppcuesPaginationData containing pageCount, currentPage index and scrollingOffset
 * that can be used to sync animations
 */
@Composable
internal fun rememberAppcuesPaginationState() = remember<State<AppcuesPaginationData>> { appcuesPaginationData }

internal val isContentVisible = MutableTransitionState(false)

@Composable
internal fun LaunchOnHideAnimationCompleted(block: () -> Unit) {
    with(remember { mutableStateOf(isContentVisible) }.value) {
        // if hide animation is completed
        if (isIdle && currentState.not()) {
            block()
        }
    }
}

// we could make this public to give more flexibility in the future
@Composable
internal fun rememberAppcuesContentVisibility() = remember { isContentVisible }

@Composable
internal fun rememberSystemMarginsState(): State<PaddingValues> {
    val density = LocalDensity.current
    val topMargin = rememberUpdatedState(newValue = with(density) { LocalContext.current.getStatusBarHeight().toDp() })
    val bottomMargin = rememberUpdatedState(newValue = with(density) { LocalContext.current.getNavigationBarHeight().toDp() })
    // will calculate status bar height and navigation bar height and return it in PaddingValues
    // this is derived state to handle possible changes to values in top and bottom margin
    return derivedStateOf { PaddingValues(top = topMargin.value, bottom = bottomMargin.value) }
}
