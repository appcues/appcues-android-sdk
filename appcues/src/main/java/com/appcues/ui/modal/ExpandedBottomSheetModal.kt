package com.appcues.ui.modal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.WindowInfo
import com.appcues.ui.extensions.WindowInfo.DeviceType.MOBILE
import com.appcues.ui.extensions.WindowInfo.DeviceType.TABLET
import com.appcues.ui.extensions.WindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.extensions.WindowInfo.Orientation.PORTRAIT
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle

@Composable
internal fun ExpandedBottomSheetModal(
    style: ComponentStyle?,
    content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit,
    windowInfo: WindowInfo,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val fullWidth = derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> 1f
                TABLET -> when (windowInfo.orientation) {
                    PORTRAIT -> 0.7f
                    LANDSCAPE -> 0.6f
                }
            }
        }

        val fullHeight = derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> when (windowInfo.orientation) {
                    PORTRAIT -> 0.95f
                    LANDSCAPE -> 1f
                }
                TABLET -> 0.7f
            }
        }

        val enterAnimation = derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> enterTransition()
                TABLET -> dialogEnterTransition()
            }
        }

        val exitAnimation = derivedStateOf {
            when (windowInfo.deviceType) {
                MOBILE -> exitTransition()
                TABLET -> dialogExitTransition()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (windowInfo.deviceType == MOBILE) Alignment.BottomCenter else Alignment.Center
        ) {
            AppcuesTraitAnimatedVisibility(
                enter = enterAnimation.value,
                exit = exitAnimation.value,
            ) {
                Surface(
                    modifier = Modifier
                        // will fill max width
                        .fillMaxWidth(fullWidth.value)
                        .fillMaxHeight(fullHeight.value)
                        // default modal style modifiers
                        .modalStyle(
                            style = style,
                            isDark = isSystemInDarkTheme(),
                            modifier = Modifier.sheetModifier(windowInfo, style, isSystemInDarkTheme()),
                        ),
                    content = { content(true, style?.getPaddings()) },
                )
            }
        }

    }
}

private fun enterTransition(): EnterTransition {
    return slideInVertically(tween(durationMillis = 300)) { it }
}

private fun exitTransition(): ExitTransition {
    return slideOutVertically(tween(durationMillis = 250)) { it } +
        fadeOut(tween(durationMillis = 200))
}
