package com.appcues.trait.appcues

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.ui.RenderViewManager
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.modal.dialogEnterTransition
import com.appcues.ui.modal.dialogExitTransition
import com.appcues.ui.modal.fullModifier
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import org.koin.core.scope.Scope

internal class EmbedTrait(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val scope: Scope,
) : ContentWrappingTrait, PresentingTrait {

    companion object {

        const val TYPE = "@appcues/embedded"
    }

    private val style = config.getConfigStyle("style")

    @Composable
    override fun WrapContent(
        content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit
    ) {
        val windowInfo = rememberAppcuesWindowInfo()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            val enterAnimation = enterTransitionDerivedOf(windowInfo)
            val exitAnimation = exitTransitionDerivedOf(windowInfo)
            val isDark = isSystemInDarkTheme()

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AppcuesTraitAnimatedVisibility(
                    visibleState = rememberAppcuesContentVisibility(),
                    enter = enterAnimation.value,
                    exit = exitAnimation.value,
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            // default modal style modifiers
                            .modalStyle(style, isDark) { Modifier.fullModifier(windowInfo, isDark, it) },
                        content = {
                            content(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                containerPadding = style.getPaddings(),
                                safeAreaInsets = PaddingValues()
                            )
                        },
                    )
                }
            }
        }
    }

    private fun enterTransitionDerivedOf(windowInfo: AppcuesWindowInfo): State<EnterTransition> {
        return derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> enterTransition()
                TABLET -> dialogEnterTransition()
            }
        }
    }

    private fun exitTransitionDerivedOf(windowInfo: AppcuesWindowInfo): State<ExitTransition> {
        return derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> exitTransition()
                TABLET -> dialogExitTransition()
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    private fun enterTransition(slideOffsetDivider: Int = 10): EnterTransition {
        return slideInVertically(tween(durationMillis = 300)) { it / slideOffsetDivider } +
            scaleIn(tween(durationMillis = 200), initialScale = 0.95f) +
            fadeIn(tween(durationMillis = 200))
    }

    @OptIn(ExperimentalAnimationApi::class)
    private fun exitTransition(slideOffsetDivider: Int = 10): ExitTransition {
        return slideOutVertically(tween(durationMillis = 250)) { it / slideOffsetDivider } +
            scaleOut(tween(durationMillis = 250), targetScale = 0.85f) +
            fadeOut(tween(durationMillis = 200))
    }

    override fun present() {
        val success = RenderViewManager(scope, renderContext).start()

        if (!success) {
            throw AppcuesTraitException("unable to create modal overlay view")
        }
    }
}
