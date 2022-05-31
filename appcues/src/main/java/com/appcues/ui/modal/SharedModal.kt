package com.appcues.ui.modal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.WindowInfo
import com.appcues.ui.extensions.WindowInfo.DeviceType.MOBILE
import com.appcues.ui.extensions.WindowInfo.DeviceType.TABLET
import com.appcues.ui.extensions.WindowInfo.Orientation.PORTRAIT
import com.appcues.ui.extensions.coloredShadow
import com.appcues.ui.extensions.styleBorder
import com.appcues.ui.extensions.styleShadow

@OptIn(ExperimentalAnimationApi::class)
internal fun dialogEnterTransition(): EnterTransition {
    return tween<Float>(durationMillis = 250).let {
        fadeIn(it) + scaleIn(it, initialScale = 0.8f)
    }
}

internal fun dialogExitTransition(): ExitTransition {
    return fadeOut(tween(durationMillis = 100))
}

internal fun Modifier.dialogModifier(style: ComponentStyle?, isDark: Boolean) =
    then(Modifier.styleShadow(style, isDark))
        .then(
            if (style?.cornerRadius != null && style.cornerRadius != 0)
                Modifier
                    .clip(RoundedCornerShape(style.cornerRadius.dp))
                    .styleBorder(style, isDark)
            else
                Modifier
        )

internal fun Modifier.sheetModifier(windowInfo: WindowInfo, style: ComponentStyle?) = then(
    when (windowInfo.deviceType) {
        MOBILE -> if (windowInfo.orientation == PORTRAIT && style?.cornerRadius != null && style.cornerRadius != 0)
            Modifier.clip(RoundedCornerShape(topStart = style.cornerRadius.dp, topEnd = style.cornerRadius.dp))
        else
            Modifier
        TABLET ->
            Modifier
                .coloredShadow(
                    color = Color(color = 0xEE777777),
                    radius = 12.dp
                )
                .clip(RoundedCornerShape(12.dp))
    }
)

internal fun Modifier.fullModifier(windowInfo: WindowInfo) = then(
    when (windowInfo.deviceType) {
        MOBILE -> Modifier
        TABLET ->
            Modifier
                .coloredShadow(
                    color = Color(color = 0xEE777777),
                    radius = 12.dp
                )
                .clip(RoundedCornerShape(12.dp))
    }
)
