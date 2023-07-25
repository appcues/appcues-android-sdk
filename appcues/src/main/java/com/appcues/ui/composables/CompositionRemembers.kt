package com.appcues.ui.composables

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.appcues.ui.presentation.AppcuesViewModel.UIState
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering

/**
 * AppcuesPaginationData is used to communicate between different traits
 * information regarding pagination.
 *
 * Every [ContentHolderTrait] should update this information if other
 * traits are supposed to react to stuff like page changes, horizontal scrolling
 * between pages, etc..
 */
public data class AppcuesPaginationData(
    /**
     * The total number of pages.
     */
    val pageCount: Int,

    /**
     * The current page.
     */
    val currentPage: Int,

    /**
     * Current scroll offset from the start of currentPage, as a ratio of the page width.
     */
    val scrollOffset: Float
)

internal class ExperienceCompositionState {

    internal val paginationData = mutableStateOf(AppcuesPaginationData(1, 0, 0.0f))

    internal val isContentVisible = MutableTransitionState(false)

    internal val isBackdropVisible = MutableTransitionState(false)
}

@Composable
internal fun LaunchOnHideAnimationCompleted(block: () -> Unit) {
    val isContentVisible = LocalExperienceCompositionState.current.isContentVisible
    with(remember { mutableStateOf(isContentVisible) }.value) {
        // if hide animation is completed
        if (isIdle && currentState.not()) {
            block()
        }
    }
}

@Composable
internal fun LaunchOnShowAnimationCompleted(block: () -> Unit) {
    val isContentVisible = LocalExperienceCompositionState.current.isContentVisible
    with(remember { mutableStateOf(isContentVisible) }.value) {
        // if show animation is completed
        if (isIdle && currentState) {
            block()
        }
    }
}

// we could make this public to give more flexibility in the future
@Composable
internal fun rememberAppcuesContentVisibility() = LocalExperienceCompositionState.current.let { remember { it.isContentVisible } }

@Composable
internal fun rememberAppcuesBackdropVisibility() = LocalExperienceCompositionState.current.let { remember { it.isBackdropVisible } }

/**
 * rememberAppcuesPaginationState is used by traits that wants to know updates about pagination data
 * returns a State of AppcuesPaginationData containing pageCount, currentPage index and scrollingOffset
 * that can be used to sync animations
 */
@Composable
internal fun rememberAppcuesPaginationState() = LocalExperienceCompositionState.current.let {
    remember<State<AppcuesPaginationData>> { it.paginationData }
}

@Composable
internal fun rememberLastRenderingState(state: State<UIState>): MutableState<Rendering?> {
    val experienceState = LocalExperienceCompositionState.current
    return remember { mutableStateOf<Rendering?>(null) }
        .apply {
            value = state.value.let { uiState ->
                if (uiState is Rendering) {
                    // if UIState is rendering then we set new value and show content
                    experienceState.isContentVisible.targetState = true
                    experienceState.isBackdropVisible.targetState = true
                    uiState
                } else {
                    // else we keep the same value and hide content to trigger dismissing animation
                    experienceState.isContentVisible.targetState = false
                    experienceState.isBackdropVisible.targetState = false
                    value
                }
            }
        }
}
