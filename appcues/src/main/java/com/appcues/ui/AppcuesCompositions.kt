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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.appcues.action.ExperienceAction

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActions = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: ((ExperienceAction) -> Unit))

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
