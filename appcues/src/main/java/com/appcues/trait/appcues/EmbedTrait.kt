package com.appcues.trait.appcues

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.EmbedTrait.TransitionType.FADE
import com.appcues.trait.appcues.EmbedTrait.TransitionType.NONE
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.modal.dialogModifier
import com.appcues.ui.presentation.EmbedViewPresenter
import org.koin.core.scope.Scope

internal class EmbedTrait(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val scope: Scope,
) : ContentWrappingTrait, PresentingTrait {

    companion object {

        const val TYPE = "@appcues/embedded"

        private const val DURATION_DEFAULT = 300
    }

    private enum class TransitionType {
        NONE, FADE
    }

    private val style = config.getConfigStyle("style")

    private val transition = config.getConfigOrDefault("transition", "none").let {
        when (it) {
            "fade" -> FADE
            else -> NONE
        }
    }

    @Composable
    override fun WrapContent(
        content: @Composable (
            modifier: Modifier,
            containerPadding: PaddingValues,
            safeAreaInsets: PaddingValues
        ) -> Unit
    ) {
        val isDark = isSystemInDarkTheme()

        AppcuesTraitAnimatedVisibility(
            visibleState = rememberAppcuesContentVisibility(),
            enter = enterTransition(),
            exit = exitTransition(),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .align(style.getBoxAlignment())
                        .fillMaxWidth()
                        // Embeds works well using the modifier set for modal (presentationStyle = dialog) for now.
                        .modalStyle(style, isDark) { Modifier.dialogModifier(it, isDark) },
                    content = {
                        content(
                            modifier = if (constraints.hasBoundedHeight) Modifier.verticalScroll(rememberScrollState()) else Modifier,
                            containerPadding = style.getPaddings(),
                            safeAreaInsets = PaddingValues()
                        )
                    },
                )
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    private fun enterTransition(): EnterTransition {
        val animationSpec: FiniteAnimationSpec<Float> = when (transition) {
            NONE -> tween(durationMillis = 0)
            FADE -> tween(durationMillis = DURATION_DEFAULT)
        }

        return scaleIn(animationSpec, initialScale = 0.95f) + fadeIn(animationSpec)
    }

    @OptIn(ExperimentalAnimationApi::class)
    private fun exitTransition(): ExitTransition {
        val animationSpec: FiniteAnimationSpec<Float> = when (transition) {
            NONE -> tween(durationMillis = 0)
            FADE -> tween(durationMillis = DURATION_DEFAULT)
        }

        return scaleOut(animationSpec, targetScale = 0.85f) + fadeOut(animationSpec)
    }

    override fun present() {
        val success = EmbedViewPresenter(scope, renderContext, scope.get()).present()

        if (!success) {
            throw AppcuesTraitException("unable to create embed view for $renderContext")
        }
    }
}
