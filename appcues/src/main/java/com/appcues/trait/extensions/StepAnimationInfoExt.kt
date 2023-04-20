package com.appcues.trait.extensions

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.appcues.trait.appcues.StepAnimationTrait
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR
import com.appcues.trait.appcues.StepAnimationTrait.StepTransitionAnimationInfo
import com.appcues.ui.composables.AppcuesStepMetadata

@Composable
internal fun rememberFloatStepAnimation(metadata: AppcuesStepMetadata): TweenSpec<Float> {
    return remember(metadata) {
        val info = metadata.current[StepAnimationTrait.STEP_TRANSITION_ANIMATION_METADATA] as StepTransitionAnimationInfo?
        when (info?.easing) {
            LINEAR -> tween(durationMillis = info.duration, easing = LinearEasing)
            EASE_IN -> tween(durationMillis = info.duration, easing = EaseIn)
            EASE_OUT -> tween(durationMillis = info.duration, easing = EaseOut)
            EASE_IN_OUT -> tween(durationMillis = info.duration, easing = EaseInOut)
            // animation with no duration is the easiest way to not use animation here
            null -> tween(durationMillis = 0, easing = LinearEasing)
        }
    }
}

@Composable
internal fun rememberColorStepAnimation(metadata: AppcuesStepMetadata): TweenSpec<Color> {
    return remember(metadata) {
        val info = metadata.current[StepAnimationTrait.STEP_TRANSITION_ANIMATION_METADATA] as StepTransitionAnimationInfo?
        when (info?.easing) {
            LINEAR -> tween(durationMillis = info.duration, easing = LinearEasing)
            EASE_IN -> tween(durationMillis = info.duration, easing = EaseIn)
            EASE_OUT -> tween(durationMillis = info.duration, easing = EaseOut)
            EASE_IN_OUT -> tween(durationMillis = info.duration, easing = EaseInOut)
            // animation with no duration is the easiest way to not use animation here
            null -> tween(durationMillis = 0, easing = LinearEasing)
        }
    }
}

@Composable
internal fun rememberDpStepAnimation(metadata: AppcuesStepMetadata): TweenSpec<Dp> {
    return remember(metadata) {
        val info = metadata.current[StepAnimationTrait.STEP_TRANSITION_ANIMATION_METADATA] as StepTransitionAnimationInfo?
        when (info?.easing) {
            LINEAR -> tween(durationMillis = info.duration, easing = LinearEasing)
            EASE_IN -> tween(durationMillis = info.duration, easing = EaseIn)
            EASE_OUT -> tween(durationMillis = info.duration, easing = EaseOut)
            EASE_IN_OUT -> tween(durationMillis = info.duration, easing = EaseInOut)
            // animation with no duration is the easiest way to not use animation here
            null -> tween(durationMillis = 0, easing = LinearEasing)
        }
    }
}
