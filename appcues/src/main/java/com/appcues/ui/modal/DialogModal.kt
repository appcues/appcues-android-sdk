package com.appcues.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.WindowInfo
import com.appcues.ui.extensions.WindowInfo.ScreenType.COMPACT
import com.appcues.ui.extensions.WindowInfo.ScreenType.EXPANDED
import com.appcues.ui.extensions.WindowInfo.ScreenType.MEDIUM
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle

private val MAX_WIDTH_COMPACT_DP = 400.dp
private val MAX_WIDTH_MEDIUM_DP = 480.dp
private val MAX_WIDTH_EXPANDED_DP = 560.dp
private val MAX_HEIGHT_COMPACT_DP = Dp.Unspecified
private val MAX_HEIGHT_MEDIUM_DP = 800.dp
private val MAX_HEIGHT_EXPANDED_DP = 900.dp
private const val SCREEN_PADDING = 0.05

@Composable
internal fun DialogModal(
    style: ComponentStyle?,
    content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit,
    windowInfo: WindowInfo
) {
    val configuration = LocalConfiguration.current
    val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
    val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp
    val isDark = isSystemInDarkTheme()

    val maxWidth = maxWidthDerivedOf(windowInfo)
    val maxHeight = maxHeightDerivedOf(windowInfo)

    AppcuesTraitAnimatedVisibility(
        enter = dialogEnterTransition(),
        exit = dialogExitTransition(),
    ) {
        Surface(
            modifier = Modifier
                .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                .fillMaxWidth()
                // container padding based on screen size
                .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin)
                // default modal style modifiers
                .modalStyle(
                    style = style,
                    isDark = isDark,
                    modifier = Modifier.dialogModifier(style, isDark),
                ),
            content = { content(false, style?.getPaddings()) },
        )
    }
}

private fun maxWidthDerivedOf(windowInfo: WindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenWidthType) {
            COMPACT -> MAX_WIDTH_COMPACT_DP
            MEDIUM -> MAX_WIDTH_MEDIUM_DP
            EXPANDED -> MAX_WIDTH_EXPANDED_DP
        }
    }
}

private fun maxHeightDerivedOf(windowInfo: WindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenHeightType) {
            COMPACT -> MAX_HEIGHT_COMPACT_DP
            MEDIUM -> MAX_HEIGHT_MEDIUM_DP
            EXPANDED -> MAX_HEIGHT_EXPANDED_DP
        }
    }
}
