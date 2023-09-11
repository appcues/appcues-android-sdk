package com.appcues.trait

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.ui.composables.LocalExperienceCompositionState

/**
 * AppcuesContentAnimatedVisibility is used to animate step content being rendered into view.
 *
 * After a step begins, it will trigger the enter animation. Prior to a step ending, it will trigger
 * the exit animation.
 */
@Composable
internal fun AppcuesContentAnimatedVisibility(
    modifier: Modifier = Modifier,
    enter: EnterTransition,
    exit: ExitTransition,
    content: @Composable() AnimatedVisibilityScope.() -> Unit
) {
    val compositionState = LocalExperienceCompositionState.current
    AnimatedVisibility(
        modifier = modifier,
        visibleState = compositionState.isContentVisible,
        enter = enter,
        exit = exit,
        content = content
    )
}
