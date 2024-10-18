package com.appcues.trait.appcues

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
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
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigColor
import com.appcues.data.model.styling.ComponentColor
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.extensions.rememberColorStepAnimation
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.LocalExperienceCompositionState
import com.appcues.ui.extensions.getColor

internal class BackdropTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait, MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/backdrop"

        const val METADATA_BACKGROUND_COLOR = "backgroundColor"
    }

    override val isBlocking = true

    override suspend fun produceMetadata(): Map<String, Any?> {
        return hashMapOf(METADATA_BACKGROUND_COLOR to config.getConfigColor("backgroundColor"))
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun BoxScope.BackdropDecorate(isBlocking: Boolean, content: @Composable BoxScope.() -> Unit) {
        val metadata = LocalAppcuesStepMetadata.current
        val animation = rememberColorStepAnimation(metadata)
        val color = rememberBackgroundColor(metadata = metadata, animationSpec = animation)

        val compositionState = LocalExperienceCompositionState.current

        AnimatedVisibility(
            visibleState = compositionState.isBackdropVisible,
            enter = enterTransition(),
            exit = exitTransition(),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    // set background color
                    .backdrop(color.value)
                    // this makes the content behind the experience non-interactive, capture all touches inside of our overlay
                    .pointerInteropFilter { false }
            )
        }

        content()
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

    @Composable
    private fun rememberBackgroundColor(metadata: AppcuesStepMetadata, animationSpec: AnimationSpec<Color>): State<Color> {
        val previousColor = rememberPreviousBackgroundColor(metadata)
        val actualColor = rememberActualBackgroundColor(metadata)

        // whenever metadata changes, set this to false
        val animateToActual = remember(metadata) { mutableStateOf(false) }

        return animateColorAsState(
            targetValue = if (animateToActual.value) actualColor else previousColor,
            animationSpec = animationSpec
        ).also {
            // whenever metadata updates, change value to true after first composition
            LaunchedEffect(metadata) { animateToActual.value = true }
        }
    }

    @Composable
    private fun rememberPreviousBackgroundColor(metadata: AppcuesStepMetadata): Color {
        val isDark = isSystemInDarkTheme()
        return remember(metadata) {
            (metadata.previous[METADATA_BACKGROUND_COLOR] as ComponentColor?).getColor(isDark) ?: Color.Transparent
        }
    }

    @Composable
    private fun rememberActualBackgroundColor(metadata: AppcuesStepMetadata): Color {
        val isDark = isSystemInDarkTheme()
        return remember(metadata) {
            (metadata.current[METADATA_BACKGROUND_COLOR] as ComponentColor?).getColor(isDark) ?: Color.Transparent
        }
    }
}
