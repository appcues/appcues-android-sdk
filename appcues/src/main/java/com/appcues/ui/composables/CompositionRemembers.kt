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
internal data class AppcuesPaginationData(
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

internal class ExperienceCompositionState(
    val paginationData: MutableState<AppcuesPaginationData> =
        mutableStateOf(AppcuesPaginationData(1, 0, 0.0f)),
    val isContentVisible: MutableTransitionState<Boolean> =
        MutableTransitionState(false),
    val isBackdropVisible: MutableTransitionState<Boolean> =
        MutableTransitionState(false),
)

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
    // Remember last known rendering state for AppcuesComposition to use when composing content
    return remember { mutableStateOf<Rendering?>(null) }.apply {
        value = state.value.let { uiState -> if (uiState is Rendering) uiState else value }
    }
}
