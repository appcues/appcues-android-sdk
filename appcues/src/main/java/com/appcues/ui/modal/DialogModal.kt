package com.appcues.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.extensions.styleShadow

private const val SCREEN_PADDING = 0.05

@Composable
internal fun DialogModal(style: ComponentStyle?, content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
    val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp
    val isDark = isSystemInDarkTheme()

    Surface(
        modifier = Modifier
            // min width and height so it doesn't look weird
            .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp)
            // container padding based on screen size
            .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin)
            // default modal style modifiers
            .modalStyle(
                style = style,
                isDark = isDark,
                modifier = Modifier
                    .styleShadow(style, isDark)
                    .dialogCorner(style),
            ),
        content = content,
    )
}

private fun Modifier.dialogCorner(style: ComponentStyle?) = this.then(
    if (style?.cornerRadius != null && style.cornerRadius != 0) Modifier
        .clip(RoundedCornerShape(style.cornerRadius.dp))
    else Modifier
)
