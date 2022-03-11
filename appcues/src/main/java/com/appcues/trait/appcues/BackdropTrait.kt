package com.appcues.trait.appcues

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigColor
import com.appcues.data.model.styling.ComponentColor
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.getColor

internal class BackdropTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait {

    private val backgroundColor = config.getConfigColor("backgroundColor")

    @Composable
    override fun Backdrop(scope: BoxScope) {
        AppcuesTraitAnimatedVisibility(
            enter = enterTransition(),
            exit = exitTransition(),
        ) {
            Spacer(
                modifier = Modifier
                    .fillMaxSize()
                    // set background color
                    .background(backgroundColor, isSystemInDarkTheme())
            )
        }
    }

    private fun Modifier.background(color: ComponentColor?, isDark: Boolean) = this.then(
        if (color != null) Modifier
            .background(color.getColor(isDark))
        else Modifier
    )

    private fun enterTransition(): EnterTransition {
        return fadeIn(tween(durationMillis = 300))
    }

    private fun exitTransition(): ExitTransition {
        return fadeOut(tween(durationMillis = 300))
    }
}
