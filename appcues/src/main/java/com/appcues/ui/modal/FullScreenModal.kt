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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle

@Composable
internal fun FullScreenModal(
    style: ComponentStyle?,
    content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit
) {
    AppcuesTraitAnimatedVisibility(
        enter = enterTransition(),
        exit = exitTransition(),
    ) {
        Surface(
            modifier = Modifier
                // will fill max size
                .fillMaxSize()
                // default modal style modifiers
                .modalStyle(
                    style = style,
                    isDark = isSystemInDarkTheme()
                ),
            content = { content(true, style?.getPaddings()) },
        )
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
