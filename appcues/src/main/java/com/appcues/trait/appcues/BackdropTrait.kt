package com.appcues.trait.appcues

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigColor
import com.appcues.data.model.styling.ComponentColor
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.rememberAppcuesBackdropVisibility
import com.appcues.ui.extensions.getColor

internal class BackdropTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait, MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/backdrop"

        const val METADATA_BACKGROUND_COLOR = "backgroundColor"
    }

    override fun produceMetadata(): Map<String, Any?> {
        return hashMapOf(METADATA_BACKGROUND_COLOR to config.getConfigColor("backgroundColor"))
    }

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        val metadata = LocalAppcuesStepMetadata.current
        val isDark = isSystemInDarkTheme()

        val previousColor = remember(metadata) {
            (metadata.previous[METADATA_BACKGROUND_COLOR] as ComponentColor?).getColor(isDark) ?: Color.Transparent
        }

        val actualColor = remember(metadata) {
            (metadata.actual[METADATA_BACKGROUND_COLOR] as ComponentColor?).getColor(isDark) ?: Color.Transparent
        }

        val duration = remember(metadata) {
            (metadata.actual[StepAnimationTrait.METADATA_ANIMATION_DURATION] as Int?) ?: StepAnimationTrait.DEFAULT_ANIMATION
        }

        val animation = remember<TweenSpec<Color>>(metadata) {
            when ((metadata.actual[StepAnimationTrait.METADATA_ANIMATION_EASING] as StepAnimationEasing?)) {
                LINEAR -> tween(durationMillis = duration, easing = LinearEasing)
                EASE_IN -> tween(durationMillis = duration, easing = EaseIn)
                EASE_OUT -> tween(durationMillis = duration, easing = EaseOut)
                EASE_IN_OUT -> tween(durationMillis = duration, easing = EaseInOut)
                // animation with no duration is the easiest way to not use animation here
                null -> tween(durationMillis = 0, easing = LinearEasing)
            }
        }

        // whenever metadata changes, set this to false
        val animateToActual = remember(metadata) { mutableStateOf(false) }

        val color = animateColorAsState(
            targetValue = if (animateToActual.value) actualColor else previousColor,
            animationSpec = animation
        ).value

        AppcuesTraitAnimatedVisibility(
            visibleState = rememberAppcuesBackdropVisibility(),
            enter = enterTransition(),
            exit = exitTransition(),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    // set background color
                    .backdrop(color)
            )
        }

        content()

        // whenever metadata updates, change value to true after first composition
        LaunchedEffect(metadata) { animateToActual.value = true }
    }

    private fun Modifier.backdrop(color: Color?) = this.then(
        if (color != null) Modifier.background(color)
        else Modifier
    )

    private fun enterTransition(): EnterTransition {
        return fadeIn(tween(durationMillis = 300))
    }

    private fun exitTransition(): ExitTransition {
        return fadeOut(tween(durationMillis = 300))
    }
}
