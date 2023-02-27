package com.appcues.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM

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
    content: @Composable (modifier: Modifier, wrapperInsets: PaddingValues) -> Unit,
    windowInfo: AppcuesWindowInfo
) {
    val configuration = LocalConfiguration.current
    val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
    val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp
    val isDark = isSystemInDarkTheme()

    val maxWidth = maxWidthDerivedOf(windowInfo)
    val maxHeight = maxHeightDerivedOf(windowInfo)

    AppcuesTraitAnimatedVisibility(
        visibleState = rememberAppcuesContentVisibility(),
        enter = dialogEnterTransition(),
        exit = dialogExitTransition(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .align(style.getBoxAlignment())
                    .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                    .fillMaxWidth()
                    // container padding based on screen size
                    .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin)
                    // default modal style modifiers
                    .modalStyle(style, isDark) { Modifier.dialogModifier(it, isDark) },
                content = {
                    content(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(style.getPaddings()),
                        wrapperInsets = PaddingValues()
                    )
                },
            )
        }
    }
}

private fun maxWidthDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenWidthType) {
            COMPACT -> MAX_WIDTH_COMPACT_DP
            MEDIUM -> MAX_WIDTH_MEDIUM_DP
            EXPANDED -> MAX_WIDTH_EXPANDED_DP
        }
    }
}

private fun maxHeightDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenHeightType) {
            COMPACT -> MAX_HEIGHT_COMPACT_DP
            MEDIUM -> MAX_HEIGHT_MEDIUM_DP
            EXPANDED -> MAX_HEIGHT_EXPANDED_DP
        }
    }
}
