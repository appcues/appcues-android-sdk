package com.appcues.trait.appcues

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.AppcuesCoroutineScope
import com.appcues.R
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.appcues.SkippableTrait.ButtonAppearance.Default
import com.appcues.trait.appcues.SkippableTrait.ButtonAppearance.Hidden
import com.appcues.trait.appcues.SkippableTrait.ButtonAppearance.Minimal
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.extensions.xShapePath
import kotlinx.coroutines.launch

internal class SkippableTrait(
    override val config: AppcuesConfigMap,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) : ContainerDecoratingTrait, BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/skippable"

        private const val CONFIG_BUTTON_APPEARANCE = "buttonAppearance"
        private const val CONFIG_IGNORE_BACKDROP_TAP = "ignoreBackdropTap"
    }

    private sealed class ButtonAppearance(val margin: Dp, val size: Dp, val padding: Dp) {

        // calculates rippleRadius based on size and padding provided
        val rippleRadius = (size / 2) - padding

        object Hidden : ButtonAppearance(0.dp, 0.dp, 0.dp)
        object Minimal : ButtonAppearance(4.dp, 30.dp, 8.dp)
        object Default : ButtonAppearance(8.dp, 30.dp, 8.dp)
    }

    override val containerComposeOrder = ContainerDecoratingType.OVERLAY

    private val buttonAppearance = when (config.getConfig<String>(CONFIG_BUTTON_APPEARANCE)) {
        "hidden" -> Hidden
        "minimal" -> Minimal
        "default" -> Default
        else -> Default
    }

    private val ignoreBackdropTap = config.getConfigOrDefault(CONFIG_IGNORE_BACKDROP_TAP, false)

    @Composable
    override fun BoxScope.DecorateContainer(containerPadding: PaddingValues, safeAreaInsets: PaddingValues) {
        val description = stringResource(id = R.string.appcues_skippable_trait_dismiss)
        Spacer(
            modifier = Modifier
                .padding(safeAreaInsets)
                .align(Alignment.TopEnd)
                .padding(buttonAppearance.margin)
                .size(buttonAppearance.size)
                .clip(CircleShape)
                .clickable(
                    onClick = {
                        appcuesCoroutineScope.launch {
                            experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)
                        }
                    },
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, radius = buttonAppearance.rippleRadius),
                    onClickLabel = description
                )
                .drawSkippableButton(buttonAppearance)
                // useful for testing and also for accessibility
                .semantics { this.contentDescription = description }
        )
    }

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        if (ignoreBackdropTap.not()) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    // add click listener but without any ripple effect.
                    .pointerInput(Unit) {
                        detectTapGestures {
                            appcuesCoroutineScope.launch {
                                experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)
                            }
                        }
                    },
            )
        }

        content()
    }

    /**
     * Apply different modifiers based on ButtonAppearance
     */
    private fun Modifier.drawSkippableButton(buttonAppearance: ButtonAppearance): Modifier {
        return this.then(
            when (buttonAppearance) {
                Hidden -> Modifier
                Default ->
                    Modifier
                        .background(Color(color = 0x54000000))
                        .padding(buttonAppearance.padding)
                        .clip(CircleShape)
                        .drawBehind {
                            xShapePath(
                                color = Color(color = 0xFFEFEFEF),
                                pathSize = buttonAppearance.size,
                                strokeWidth = 1.5.dp,
                            ).also { drawPath(path = it, color = Color.Transparent) }
                        }
                Minimal ->
                    Modifier
                        .padding(buttonAppearance.padding)
                        .clip(CircleShape)
                        .drawBehind {
                            xShapePath(
                                color = Color(color = 0xFFB2B2B2),
                                pathSize = buttonAppearance.size,
                                strokeWidth = 1.5.dp,
                            ).also { drawPath(path = it, color = Color.Transparent, blendMode = BlendMode.Difference) }
                        }
            }
        )
    }
}
