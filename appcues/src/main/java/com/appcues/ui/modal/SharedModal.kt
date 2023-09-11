package com.appcues.ui.modal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.getCornerRadius
import com.appcues.ui.extensions.styleCorner
import com.appcues.ui.extensions.styleShadow
import com.appcues.ui.modal.TransitionEdge.BOTTOM
import com.appcues.ui.modal.TransitionEdge.CENTER
import com.appcues.ui.modal.TransitionEdge.LEADING
import com.appcues.ui.modal.TransitionEdge.TOP
import com.appcues.ui.modal.TransitionEdge.TRAILING
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT

@OptIn(ExperimentalAnimationApi::class)
internal fun dialogEnterTransition(): EnterTransition {
    return tween<Float>(durationMillis = 250).let {
        fadeIn(it) + scaleIn(it, initialScale = 0.8f)
    }
}

internal fun dialogExitTransition(): ExitTransition {
    return fadeOut(tween(durationMillis = 100))
}

internal fun slideOutEnterTransition(initial: TransitionEdge, horizontalPadding: Int, verticalPadding: Int): EnterTransition {
    val slide = slideIn(
        initialOffset = { initial.toOffset(it, horizontalPadding, verticalPadding) },
        animationSpec = tween(durationMillis = 1000)
    )

    return if (initial == CENTER) {
        slide + fadeIn(tween(durationMillis = 1000))
    } else {
        slide
    }
}

internal fun slideOutExitTransition(target: TransitionEdge, horizontalPadding: Int, verticalPadding: Int): ExitTransition {
    val slide = slideOut(
        targetOffset = { target.toOffset(it, horizontalPadding, verticalPadding) },
        animationSpec = tween(durationMillis = 300)
    )

    return if (target == CENTER) {
        slide + fadeOut(tween(durationMillis = 300))
    } else {
        slide
    }
}

private fun TransitionEdge.toOffset(fullSize: IntSize, horizontalPadding: Int, verticalPadding: Int): IntOffset {
    return when (this) {
        LEADING -> IntOffset(-fullSize.width - horizontalPadding, 0)
        TRAILING -> IntOffset(fullSize.width + horizontalPadding, 0)
        TOP -> IntOffset(0, -fullSize.height - verticalPadding)
        BOTTOM -> IntOffset(0, fullSize.height + verticalPadding)
        CENTER -> IntOffset(0, fullSize.height / 2)
    }
}

internal fun Modifier.dialogModifier(style: ComponentStyle, isDark: Boolean) =
    then(
        Modifier
            .styleShadow(style, isDark)
            .styleCorner(style)
    )

internal fun Modifier.sheetModifier(windowInfo: AppcuesWindowInfo, isDark: Boolean, style: ComponentStyle) = then(
    when (windowInfo.deviceType) {
        MOBILE -> style.getCornerRadius().let { cornerRadius ->
            if (windowInfo.orientation == PORTRAIT && cornerRadius != 0.dp)
                Modifier.clip(RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius))
            else
                Modifier
        }
        TABLET ->
            Modifier
                .styleShadow(style, isDark)
                .styleCorner(style)
    }
)

internal fun Modifier.fullModifier(windowInfo: AppcuesWindowInfo, isDark: Boolean, style: ComponentStyle) = then(
    when (windowInfo.deviceType) {
        MOBILE -> Modifier
        TABLET ->
            Modifier
                .styleShadow(style, isDark)
                .styleCorner(style)
    }
)
