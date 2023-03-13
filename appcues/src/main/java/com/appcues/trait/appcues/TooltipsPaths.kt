package com.appcues.trait.appcues

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.trait.appcues.PointerHorizontalAlignment.CENTER
import com.appcues.trait.appcues.PointerHorizontalAlignment.END
import com.appcues.trait.appcues.PointerHorizontalAlignment.START
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.None
import com.appcues.trait.appcues.TooltipPointerPosition.Top
import kotlin.math.max

internal data class PointerAlignment(
    val verticalAlignment: PointerVerticalAlignment = PointerVerticalAlignment.CENTER,
    val horizontalAlignment: PointerHorizontalAlignment = CENTER,
)

internal enum class PointerVerticalAlignment {
    TOP, CENTER, BOTTOM
}

internal enum class PointerHorizontalAlignment {
    START, CENTER, END
}

internal data class ContainerCornerRadius(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp,
)

internal sealed class TooltipPointerPosition {

    private val _alignment = mutableStateOf(PointerAlignment())
    val alignment: State<PointerAlignment> = _alignment
    fun verticalAlignment(alignment: PointerVerticalAlignment) {
        this._alignment.value = PointerAlignment(verticalAlignment = alignment)
    }

    fun horizontalAlignment(alignment: PointerHorizontalAlignment) {
        this._alignment.value = PointerAlignment(horizontalAlignment = alignment)
    }

    abstract fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius

    object Top : TooltipPointerPosition() {

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = if (alignment.value.horizontalAlignment == START) 0.dp else cornerRadius,
                topEnd = if (alignment.value.horizontalAlignment == END) 0.dp else cornerRadius,
                bottomEnd = cornerRadius,
                bottomStart = cornerRadius
            )
        }
    }

    object Bottom : TooltipPointerPosition() {

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomStart = if (alignment.value.horizontalAlignment == START) 0.dp else cornerRadius,
                bottomEnd = if (alignment.value.horizontalAlignment == END) 0.dp else cornerRadius
            )
        }
    }

    object None : TooltipPointerPosition() {

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomEnd = cornerRadius,
                bottomStart = cornerRadius
            )
        }
    }
}

internal data class TooltipSettings(
    val hidePointer: Boolean,
    val tooltipPointerPosition: TooltipPointerPosition,
    val distance: Dp,
    val pointerBaseDp: Dp,
    val pointerLengthDp: Dp,
    val pointerBasePx: Float,
    val pointerLengthPx: Float
) {

    val pointerBaseCenterPx = pointerBasePx / 2
    val pointerOffsetX = mutableStateOf(0.dp)
}

internal data class TooltipContainerDimens(
    val widthDp: Dp,
    val heightDp: Dp,
    val widthPx: Float,
    val heightPx: Float,
    val cornerRadius: Dp,
)

internal fun getTooltipSettings(
    density: Density,
    position: TooltipPointerPosition,
    distance: Dp,
    pointerBaseDp: Dp,
    pointerLengthDp: Dp,
): TooltipSettings {
    return TooltipSettings(
        hidePointer = false,
        tooltipPointerPosition = position,
        distance = distance,
        pointerBaseDp = pointerBaseDp,
        pointerLengthDp = pointerLengthDp,
        pointerBasePx = with(density) { pointerBaseDp.toPx() },
        pointerLengthPx = with(density) { pointerLengthDp.toPx() }
    )
}

@Composable
internal fun tooltipPath(
    tooltipSettings: TooltipSettings,
    containerDimens: TooltipContainerDimens?,
    animationFloatSpec: AnimationSpec<Float>,
    animationDpSpec: AnimationSpec<Dp>,
): Path {
    return Path().apply {
        // if containerDimens is null we don't to path the tooltip yet
        if (containerDimens == null) return@apply

        op(
            path1 = getTooltipPointerPath(containerDimens, tooltipSettings, animationFloatSpec),
            path2 = getContainerPath(containerDimens, tooltipSettings, animationDpSpec),
            operation = PathOperation.Union
        )
    }
}

@Composable
private fun getTooltipPointerPath(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    animationSpec: AnimationSpec<Float>,
): Path {
    val density = LocalDensity.current

    val pointerOffsetX = calculatePointerXOffset(
        containerDimens = containerDimens,
        tooltipSettings = tooltipSettings,
        pointerOffsetX = with(density) { tooltipSettings.pointerOffsetX.value.toPx() },
        cornerRadius = with(density) { containerDimens.cornerRadius.toPx() },
    )

    // vertical pointer implementation, probably in the future make this a separate method and add
    // another one to generate a horizontal pointer for leading and trailing positions.
    return Path().apply {

        TooltipPointerPath(tooltipSettings, pointerOffsetX.second, animationSpec)

        val pathOffsetX = animateFloatAsState(pointerOffsetX.first, animationSpec)
        val pathOffsetY = if (tooltipSettings.tooltipPointerPosition is Bottom)
            containerDimens.heightPx - tooltipSettings.pointerLengthPx else tooltipSettings.pointerLengthPx

        // Move path to correct place
        translate(Offset(pathOffsetX.value, pathOffsetY))
    }
}

private fun calculatePointerXOffset(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    pointerOffsetX: Float,
    cornerRadius: Float,
): Pair<Float, Float> {
    val tempPointerOffset = (containerDimens.widthPx / 2) - tooltipSettings.pointerBaseCenterPx + pointerOffsetX

    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + cornerRadius
    val maxPointerOffset = max((containerDimens.widthPx - tooltipSettings.pointerBasePx), 0f)
    val maxPointerOffsetCornerRadius = maxPointerOffset - cornerRadius

    // figure out the offset of the pointer and offset for the tooltip pointer when hits the edges
    var tooltipPointerOffsetX = 0f
    val offsetX = when {
        tempPointerOffset > minPointerOffsetCornerRadius && tempPointerOffset < maxPointerOffsetCornerRadius -> {
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(CENTER)
            tempPointerOffset
        }
        tempPointerOffset > minPointerOffset && tempPointerOffset < maxPointerOffset -> {
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(CENTER)
            tempPointerOffset.coerceIn(minPointerOffsetCornerRadius..maxPointerOffsetCornerRadius).toFloat()
        }
        tempPointerOffset <= minPointerOffset -> {
            tooltipPointerOffsetX = -tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(START)
            minPointerOffset
        }
        tempPointerOffset >= maxPointerOffset -> {
            tooltipPointerOffsetX = tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(END)
            maxPointerOffset
        }
        else -> 0f
    }
    return Pair(offsetX, tooltipPointerOffsetX)
}

@Composable
private fun Path.TooltipPointerPath(
    pointerSettings: TooltipSettings,
    pointerOffsetX: Float,
    animationSpec: AnimationSpec<Float>
) {
    val animatedTipXOffset = animateFloatAsState(pointerOffsetX, animationSpec)
    val animatedTipLength = animateFloatAsState(
        targetValue = when (pointerSettings.tooltipPointerPosition) {
            is Top -> -pointerSettings.pointerLengthPx
            is Bottom -> pointerSettings.pointerLengthPx
            None -> 0f
        },
        animationSpec = animationSpec
    )

    reset()
    lineTo(x = 0f, y = 0f)
    lineTo(x = pointerSettings.pointerBaseCenterPx + animatedTipXOffset.value, y = animatedTipLength.value)
    lineTo(x = pointerSettings.pointerBasePx, y = 0f)
    close()
}

@Composable
private fun getContainerPath(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    animationSpec: AnimationSpec<Dp>,
): Path {
    val containerCornerRadius = remember(tooltipSettings.tooltipPointerPosition.alignment.value) {
        tooltipSettings.tooltipPointerPosition.toContainerCornerRadius(containerDimens.cornerRadius)
    }

    val cornerTopStart = animateDpAsState(containerCornerRadius.topStart, animationSpec)
    val cornerTopEnd = animateDpAsState(containerCornerRadius.topEnd, animationSpec)
    val cornerBottomStart = animateDpAsState(containerCornerRadius.bottomStart, animationSpec)
    val cornerBottomEnd = animateDpAsState(containerCornerRadius.bottomEnd, animationSpec)

    return Path().apply {
        // takes into account the length of the tooltip pointer when calculating new container size
        val containerSizePx = Size(containerDimens.widthPx, containerDimens.heightPx - tooltipSettings.pointerLengthPx)

        val containerOffsetPx = Offset(
            x = 0f,
            y = when (tooltipSettings.tooltipPointerPosition) {
                // offset entire content rectangle in case tooltip is at the top
                is Top -> tooltipSettings.pointerLengthPx
                else -> 0f
            }
        )

        addPath(
            Path().apply {
                addOutline(
                    RoundedCornerShape(
                        topStart = cornerTopStart.value,
                        topEnd = cornerTopEnd.value,
                        bottomEnd = cornerBottomEnd.value,
                        bottomStart = cornerBottomStart.value
                    ).createOutline(
                        containerSizePx,
                        LocalLayoutDirection.current,
                        LocalDensity.current
                    )
                )
            },
            containerOffsetPx
        )
    }
}
