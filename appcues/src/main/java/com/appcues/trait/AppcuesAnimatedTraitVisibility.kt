package com.appcues.trait

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.ui.composables.rememberAppcuesContentVisibility

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
