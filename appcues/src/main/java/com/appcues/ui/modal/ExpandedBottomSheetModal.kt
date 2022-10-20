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
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT

private const val WIDTH_MOBILE = 1f
private const val WIDTH_TABLET_PORTRAIT = 0.7f
private const val WIDTH_TABLET_LANDSCAPE = 0.6f
private const val HEIGHT_MOBILE_PORTRAIT = 0.95f
private const val HEIGHT_MOBILE_LANDSCAPE = 1f
private const val HEIGHT_TABLET = 0.7f

@Composable
internal fun ExpandedBottomSheetModal(
    style: ComponentStyle?,
    content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit,
    windowInfo: AppcuesWindowInfo,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val width = widthDerivedOf(windowInfo)
        val height = heightDerivedOf(windowInfo)
        val enterAnimation = enterTransitionDerivedOf(windowInfo)
        val exitAnimation = exitTransitionDerivedOf(windowInfo)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = if (windowInfo.deviceType == MOBILE) Alignment.BottomCenter else Alignment.Center
        ) {
            AppcuesTraitAnimatedVisibility(
                visibleState = rememberAppcuesContentVisibility(),
                enter = enterAnimation.value,
                exit = exitAnimation.value,
            ) {
                Surface(
                    modifier = Modifier
                        // will fill max width
                        .fillMaxWidth(width.value)
                        .fillMaxHeight(height.value)
                        // default modal style modifiers
                        .modalStyle(
                            style = style,
                            isDark = isSystemInDarkTheme(),
                            modifier = Modifier.sheetModifier(windowInfo, style),
                        ),
                    content = { content(true, style?.getPaddings()) },
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
            MOBILE -> when (windowInfo.orientation) {
                PORTRAIT -> HEIGHT_MOBILE_PORTRAIT
                LANDSCAPE -> HEIGHT_MOBILE_LANDSCAPE
            }
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

private fun enterTransition(): EnterTransition {
    return slideInVertically(tween(durationMillis = 300)) { it }
}

private fun exitTransition(): ExitTransition {
    return slideOutVertically(tween(durationMillis = 250)) { it } +
        fadeOut(tween(durationMillis = 200))
}
