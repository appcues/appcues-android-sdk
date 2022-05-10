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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle

@Composable
internal fun ExpandedBottomSheetModal(
    style: ComponentStyle?,
    content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        AppcuesTraitAnimatedVisibility(
            enter = enterTransition(),
            exit = exitTransition(),
        ) {
            Surface(
                modifier = Modifier
                    // will fill max width
                    .fillMaxWidth()
                    // will fill height in 95%
                    .fillMaxHeight(fraction = 0.95f)
                    // default modal style modifiers
                    .modalStyle(
                        style = style,
                        isDark = isSystemInDarkTheme(),
                        modifier = Modifier.bottomSheetCorner(style),
                    ),
                content = { content(true, style?.getPaddings()) },
            )
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

private fun Modifier.bottomSheetCorner(style: ComponentStyle?) = this.then(
    if (style?.cornerRadius != null && style.cornerRadius != 0) Modifier
        .clip(RoundedCornerShape(topStart = style.cornerRadius.dp, topEnd = style.cornerRadius.dp))
    else Modifier
)
