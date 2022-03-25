package com.appcues.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.appcues.action.ExperienceAction

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActions = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: (ExperienceAction) -> Unit)

/**
 * LocalAppcuesPagination is used to report back any page change that
 * happened that is coming from outside of our internal SDK logic.
 * Usually used by traits that when dealing with multi-page containers
 */
val LocalAppcuesPagination = compositionLocalOf { AppcuesPagination {} }

data class AppcuesPagination(val onPageChanged: (Int) -> Unit)

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
fun rememberAppcuesPaginationState() = remember<State<AppcuesPaginationData>> { appcuesPaginationData }

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

/**
 * AppcuesTraitAnimatedVisibility is used to animate traits based on internal state of Appcues SDK
 *
 * Right after the ExperiencePresentingTrait starts the step it will trigger the enter animation, also
 * before the ExperiencePresentingTrait is finished it will go over the exit animation as well.
 */
@Composable
fun AppcuesTraitAnimatedVisibility(
    modifier: Modifier = Modifier,
    enter: EnterTransition = fadeIn() + expandIn(),
    exit: ExitTransition = fadeOut() + shrinkOut(),
    content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = rememberAppcuesContentVisibility(),
        enter = enter,
        exit = exit,
        content = content
    )
}
