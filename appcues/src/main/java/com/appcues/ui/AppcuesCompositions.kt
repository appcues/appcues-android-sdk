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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action
import java.util.UUID

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActionDelegate = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: (ExperienceAction) -> Unit)

internal val LocalAppcuesActions = staticCompositionLocalOf<Map<UUID, List<Action>>> { hashMapOf() }

/**
 * LocalAppcuesPagination is used to report back any page change that
 * happened that is coming from outside of our internal SDK logic.
 * Usually used by traits that when dealing with multi-page containers
 */
internal val LocalAppcuesPaginationDelegate = compositionLocalOf { AppcuesPagination {} }

internal data class AppcuesPagination(val onPageChanged: (Int) -> Unit)

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
