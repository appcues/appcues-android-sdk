package com.appcues.ui.modal

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.AppcuesContentAnimatedVisibility
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT

private const val WIDTH_MOBILE = 1f
private const val WIDTH_TABLET_PORTRAIT = 0.9f
private const val WIDTH_TABLET_LANDSCAPE = 0.7f
private const val HEIGHT_MOBILE = 1f
private const val HEIGHT_TABLET = 0.85f

@Composable
internal fun FullScreenModal(
    style: ComponentStyle?,
    content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit,
    windowInfo: AppcuesWindowInfo,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val fullWidth = widthDerivedOf(windowInfo)
        val fullHeight = heightDerivedOf(windowInfo)
        val enterAnimation = enterTransitionDerivedOf(windowInfo)
        val exitAnimation = exitTransitionDerivedOf(windowInfo)
        val isDark = isSystemInDarkTheme()

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (windowInfo.deviceType == MOBILE) Alignment.BottomCenter else Alignment.Center
        ) {
            AppcuesContentAnimatedVisibility(
                enter = enterAnimation.value,
                exit = exitAnimation.value,
            ) {
                Surface(
                    modifier = Modifier
                        // will fill max width
                        .fillMaxWidth(fullWidth.value)
                        .fillMaxHeight(fullHeight.value)
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

private fun widthDerivedOf(windowInfo: AppcuesWindowInfo): State<Float> {
    return derivedStateOf {
        when (windowInfo.deviceType) {
            MOBILE -> WIDTH_MOBILE
            TABLET -> when (windowInfo.orientation) {
                PORTRAIT -> WIDTH_TABLET_PORTRAIT
                LANDSCAPE -> WIDTH_TABLET_LANDSCAPE
            }
        }
    }
}

private fun heightDerivedOf(windowInfo: AppcuesWindowInfo): State<Float> {
    return derivedStateOf {
        when (windowInfo.deviceType) {
            MOBILE -> HEIGHT_MOBILE
            TABLET -> HEIGHT_TABLET
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
