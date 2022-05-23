package com.appcues.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
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

    val maxWidth = derivedStateOf {
        when (windowInfo.screenWidthType) {
            COMPACT -> 400.dp
            MEDIUM -> 480.dp
            EXPANDED -> 560.dp
        }
    }

    val maxHeight = derivedStateOf {
        when (windowInfo.screenHeightType) {
            COMPACT -> Dp.Unspecified
            MEDIUM -> 800.dp
            EXPANDED -> 900.dp
        }
    }

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
