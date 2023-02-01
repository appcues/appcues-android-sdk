package com.appcues.trait.appcues

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_END
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_START
import com.appcues.trait.appcues.TooltipPointerPosition.NONE
import com.appcues.trait.appcues.TooltipPointerPosition.TOP
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_END
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_START
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.util.ne

internal enum class TooltipPointerPosition {
    TOP, TOP_START, TOP_END, BOTTOM, BOTTOM_START, BOTTOM_END, NONE
}

internal data class TooltipSettings(
    val hidePointer: Boolean,
    val pointerPosition: TooltipPointerPosition,
    val pointerBaseDp: Dp,
    val pointerLengthDp: Dp,
    val pointerBasePx: Float,
    val pointerLengthPx: Float,
) {

    val pointerBaseCenterPx = pointerBasePx / 2
    val pointerOffsetX = mutableStateOf(0.dp)
}

internal data class TooltipContainerDimens(
    val widthDp: Dp,
    val heightDp: Dp,
    val widthPx: Float,
    val heightPx: Float
)

internal fun getTooltipSettings(
    density: Density,
    position: TooltipPointerPosition,
    pointerBaseDp: Dp,
    pointerLengthDp: Dp,
): TooltipSettings {
    return TooltipSettings(
        hidePointer = false,
        pointerPosition = position,
        pointerBaseDp = pointerBaseDp,
        pointerLengthDp = pointerLengthDp,
        pointerBasePx = with(density) { pointerBaseDp.toPx() },
        pointerLengthPx = with(density) { pointerLengthDp.toPx() }
    )
}

internal fun getPointerPosition(
    windowInfo: AppcuesWindowInfo,
    targetRect: Rect?,
): TooltipPointerPosition {
    // Figure out where to position the tooltip
    return when {
        targetRect == null -> NONE
        targetRect.center.y.dp < windowInfo.heightDp / 2 -> TOP
        else -> BOTTOM
    }
}

@Composable
internal fun tooltipPath(
    tooltipSettings: TooltipSettings,
    containerDimens: TooltipContainerDimens?,
    style: ComponentStyle?,
    animationSpec: AnimationSpec<Float>,
): Path {
    return Path().apply {
        // if containerDimens is null we don't to path the tooltip yet
        if (containerDimens == null) return@apply

        // information used by both container path and tooltip pointer path
        val containerRealSize = Size(containerDimens.widthPx, containerDimens.heightPx - tooltipSettings.pointerLengthPx)

        op(
            path1 = getTooltipPointerPath(style, containerDimens, tooltipSettings, containerRealSize, animationSpec),
            path2 = getContainerPath(tooltipSettings, style, containerRealSize),
            operation = PathOperation.Union
        )
    }
}

@Composable
private fun getTooltipPointerPath(
    style: ComponentStyle?,
    containerDimens: TooltipContainerDimens,
    pointerSettings: TooltipSettings,
    containerSize: Size,
    animationSpec: AnimationSpec<Float>,
): Path {
    val density = LocalDensity.current

    // mapping all values to px
    val cornerRadius = with(density) { style?.cornerRadius?.dp?.toPx() ?: 0f }
    val pointerOffset = with(density) { pointerSettings.pointerOffsetX.value.toPx() } ?: 0f
    val offsetX = (containerDimens.widthPx / 2) - pointerSettings.pointerBaseCenterPx + pointerOffset

    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + cornerRadius
    val maxPointerOffset = (containerDimens.widthPx - pointerSettings.pointerBasePx)
        .let { return@let if (it < 0) 0f else it }
    val maxPointerOffsetCornerRadius = maxPointerOffset - cornerRadius

    // vertical pointer implementation, probably in the future make this a separate method and add
    // another one to generate a horizontal pointer for leading and trailing positions.
    return Path().apply {
        val pathOffsetY = animateFloatAsState(
            targetValue = if (pointerSettings.pointerPosition == BOTTOM)
                containerSize.height else pointerSettings.pointerLengthPx,
            animationSpec = animationSpec,
        )

        val pathOffsetX = animateFloatAsState(
            targetValue = offsetX.coerceIn(minPointerOffset..maxPointerOffset),
            animationSpec = animationSpec,
        )

        val pointerTipOffsetX = animateFloatAsState(
            targetValue = if (offsetX < minPointerOffsetCornerRadius) {
                -pointerSettings.pointerBaseCenterPx
            } else if (offsetX > maxPointerOffsetCornerRadius) {
                pointerSettings.pointerBaseCenterPx
            } else 0f,
            animationSpec = animationSpec,
        )

        TooltipPointerPath(pointerSettings, pointerTipOffsetX.value)

        // Move path to correct place
        translate(Offset(pathOffsetX.value, pathOffsetY.value))
    }
}

@Composable
private fun Path.TooltipPointerPath(
    pointerSettings: TooltipSettings,
    pointerOffsetX: Float
) {
    animateFloatAsState(
        targetValue = when (pointerSettings.pointerPosition) {
            TOP, TOP_START, TOP_END -> -pointerSettings.pointerLengthPx
            BOTTOM, BOTTOM_START, BOTTOM_END -> pointerSettings.pointerLengthPx
            NONE -> 0f
        }
    ).let {
        reset()
        lineTo(x = 0f, y = 0f)
        lineTo(x = pointerSettings.pointerBaseCenterPx + pointerOffsetX, y = it.value)
        lineTo(x = pointerSettings.pointerBasePx, y = 0f)
        close()
    }
}

@Composable
private fun getContainerPath(
    pointerSettings: TooltipSettings,
    style: ComponentStyle?,
    containerSizeWithPointer: Size,
): Path {
    return Path().apply {
        val containerOffsetPx =
            Offset(
                0f,
                y = when (pointerSettings.pointerPosition) {
                    // offset entire content rectangle in case tooltip is at the top
                    TOP, TOP_START, TOP_END -> pointerSettings.pointerLengthPx
                    else -> 0f
                }
            )

        if (style != null && style.cornerRadius ne 0.0) {
            val cornerTopStart =
                animateDpAsState(targetValue = if (pointerSettings.pointerPosition == TOP_START) 0.dp else style.cornerRadius.dp)
            val cornerTopEnd =
                animateDpAsState(targetValue = if (pointerSettings.pointerPosition == TOP_END) 0.dp else style.cornerRadius.dp)
            val cornerBottomStart =
                animateDpAsState(targetValue = if (pointerSettings.pointerPosition == BOTTOM_START) 0.dp else style.cornerRadius.dp)
            val cornerBottomEnd =
                animateDpAsState(targetValue = if (pointerSettings.pointerPosition == BOTTOM_END) 0.dp else style.cornerRadius.dp)
            addPath(
                Path().apply {
                    addOutline(
                        RoundedCornerShape(
                            topStart = cornerTopStart.value,
                            topEnd = cornerTopEnd.value,
                            bottomEnd = cornerBottomEnd.value,
                            bottomStart = cornerBottomStart.value
                        ).createOutline(
                            containerSizeWithPointer,
                            LocalLayoutDirection.current,
                            LocalDensity.current
                        )
                    )
                },
                containerOffsetPx
            )
        } else {
            addRect(Rect(containerOffsetPx, containerSizeWithPointer))
        }
    }
}
