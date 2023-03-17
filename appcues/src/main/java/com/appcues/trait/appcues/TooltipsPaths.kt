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
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.Left
import com.appcues.trait.appcues.TooltipPointerPosition.Right
import com.appcues.trait.appcues.TooltipPointerPosition.Top
import kotlin.math.max

internal data class PointerAlignment(
    val verticalAlignment: PointerVerticalAlignment = PointerVerticalAlignment.CENTER,
    val horizontalAlignment: PointerHorizontalAlignment = PointerHorizontalAlignment.CENTER,
)

internal enum class PointerVerticalAlignment {
    TOP, CENTER, BOTTOM
}

internal enum class PointerHorizontalAlignment {
    LEFT, CENTER, RIGHT
}

internal data class ContainerCornerRadius(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp,
)

internal sealed class TooltipPointerPosition {

    abstract val isVertical: Boolean

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

        override val isVertical = true

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = if (alignment.value.horizontalAlignment == PointerHorizontalAlignment.LEFT) 0.dp else cornerRadius,
                topEnd = if (alignment.value.horizontalAlignment == PointerHorizontalAlignment.RIGHT) 0.dp else cornerRadius,
                bottomEnd = cornerRadius,
                bottomStart = cornerRadius
            )
        }
    }

    object Bottom : TooltipPointerPosition() {

        override val isVertical = true

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = cornerRadius,
                topEnd = cornerRadius,
                bottomStart = if (alignment.value.horizontalAlignment == PointerHorizontalAlignment.LEFT) 0.dp else cornerRadius,
                bottomEnd = if (alignment.value.horizontalAlignment == PointerHorizontalAlignment.RIGHT) 0.dp else cornerRadius
            )
        }
    }

    object Left : TooltipPointerPosition() {

        override val isVertical = false

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = if (alignment.value.verticalAlignment == PointerVerticalAlignment.TOP) 0.dp else cornerRadius,
                topEnd = cornerRadius,
                bottomEnd = cornerRadius,
                bottomStart = if (alignment.value.verticalAlignment == PointerVerticalAlignment.BOTTOM) 0.dp else cornerRadius
            )
        }
    }

    object Right : TooltipPointerPosition() {

        override val isVertical = false

        override fun toContainerCornerRadius(cornerRadius: Dp): ContainerCornerRadius {
            return ContainerCornerRadius(
                topStart = cornerRadius,
                topEnd = if (alignment.value.verticalAlignment == PointerVerticalAlignment.TOP) 0.dp else cornerRadius,
                bottomEnd = if (alignment.value.verticalAlignment == PointerVerticalAlignment.BOTTOM) 0.dp else cornerRadius,
                bottomStart = cornerRadius,
            )
        }
    }

    object None : TooltipPointerPosition() {

        override val isVertical = false

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
    val pointerOffsetY = mutableStateOf(0.dp)
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
    val cornerRadiusPx = with(density) { containerDimens.cornerRadius.toPx() }

    val (pointerOffsetX, verticalTipOffset) = calculatePointerXOffset(
        containerDimens = containerDimens,
        tooltipSettings = tooltipSettings,
        pointerOffsetX = with(density) { tooltipSettings.pointerOffsetX.value.toPx() },
        cornerRadiusPx = cornerRadiusPx,
    )

    val (pointerOffsetY, horizontalTipOffset) = calculatePointerYOffset(
        containerDimens = containerDimens,
        tooltipSettings = tooltipSettings,
        pointerOffsetY = with(density) { tooltipSettings.pointerOffsetY.value.toPx() },
        cornerRadiusPx = cornerRadiusPx,
    )

    val tipLength = animateFloatAsState(
        targetValue = when (tooltipSettings.tooltipPointerPosition) {
            Top, Left -> -tooltipSettings.pointerLengthPx
            Bottom, Right -> tooltipSettings.pointerLengthPx
            else -> 0f
        },
        animationSpec = animationSpec
    )

    // vertical pointer implementation, probably in the future make this a separate method and add
    // another one to generate a horizontal pointer for leading and trailing positions.
    return Path().apply {

        val animatedPointerOffsetX = animateFloatAsState(pointerOffsetX, animationSpec)
        val animatedPointerOffsetY = animateFloatAsState(pointerOffsetY, animationSpec)

        if (tooltipSettings.tooltipPointerPosition.isVertical) {
            TooltipVerticalPointerPath(tipLength.value, tooltipSettings.pointerBasePx, verticalTipOffset, animationSpec)
            translate(Offset(animatedPointerOffsetX.value, pointerOffsetY))
        } else {
            TooltipHorizontalPointerPath(tipLength.value, tooltipSettings.pointerBasePx, horizontalTipOffset, animationSpec)
            translate(Offset(pointerOffsetX, animatedPointerOffsetY.value))
        }
    }
}

private fun calculatePointerXOffset(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    pointerOffsetX: Float,
    cornerRadiusPx: Float,
): Pair<Float, Float> {
    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + cornerRadiusPx
    val maxPointerOffset = max((containerDimens.widthPx - tooltipSettings.pointerBasePx), 0f)
    val maxPointerOffsetCornerRadius = maxPointerOffset - cornerRadiusPx

    // figure out the offset of the pointer and offset for the tooltip pointer when hits the edges
    var pointerTipOffset = 0f
    var pointerOffset = (containerDimens.widthPx / 2) - tooltipSettings.pointerBaseCenterPx + pointerOffsetX

    when {
        tooltipSettings.tooltipPointerPosition is Right -> {
            pointerOffset = containerDimens.widthPx - tooltipSettings.pointerLengthPx
        }
        tooltipSettings.tooltipPointerPosition is Left -> {
            pointerOffset = tooltipSettings.pointerLengthPx
        }
        pointerOffset > minPointerOffsetCornerRadius && pointerOffset < maxPointerOffsetCornerRadius -> {
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.CENTER)
        }
        pointerOffset > minPointerOffset && pointerOffset < maxPointerOffset -> {
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.CENTER)
            pointerOffset = pointerOffset.coerceIn(minPointerOffsetCornerRadius..maxPointerOffsetCornerRadius).toFloat()
        }
        pointerOffset <= minPointerOffset -> {
            pointerTipOffset = -tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.LEFT)
            pointerOffset = minPointerOffset
        }
        pointerOffset >= maxPointerOffset -> {
            pointerTipOffset = tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.RIGHT)
            pointerOffset = maxPointerOffset
        }
    }

    return Pair(pointerOffset, pointerTipOffset)
}

private fun calculatePointerYOffset(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    pointerOffsetY: Float,
    cornerRadiusPx: Float
): Pair<Float, Float> {
    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + cornerRadiusPx
    val maxPointerOffset = max((containerDimens.heightPx - tooltipSettings.pointerBasePx), 0f)
    val maxPointerOffsetCornerRadius = maxPointerOffset - cornerRadiusPx

    // figure out the offset of the pointer and offset for the tooltip pointer when hits the edges
    var pointerTipOffset = 0f
    var pointerOffset = (containerDimens.heightPx / 2) - tooltipSettings.pointerBaseCenterPx + pointerOffsetY

    when {
        tooltipSettings.tooltipPointerPosition is Bottom -> {
            pointerOffset = containerDimens.heightPx - tooltipSettings.pointerLengthPx
        }
        tooltipSettings.tooltipPointerPosition is Top -> {
            pointerOffset = tooltipSettings.pointerLengthPx
        }
        pointerOffset > minPointerOffsetCornerRadius && pointerOffset < maxPointerOffsetCornerRadius -> {
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.CENTER)
        }
        pointerOffset > minPointerOffset && pointerOffset < maxPointerOffset -> {
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.CENTER)
            pointerOffset = pointerOffset.coerceIn(minPointerOffsetCornerRadius..maxPointerOffsetCornerRadius).toFloat()
        }
        pointerOffset <= minPointerOffset -> {
            pointerTipOffset = -tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.TOP)
            pointerOffset = minPointerOffset
        }
        pointerOffset >= maxPointerOffset -> {
            pointerTipOffset = tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.BOTTOM)
            pointerOffset = maxPointerOffset
        }
    }

    return Pair(pointerOffset, pointerTipOffset)
}

@Composable
private fun Path.TooltipVerticalPointerPath(
    length: Float,
    base: Float,
    tipOffset: Float,
    animationSpec: AnimationSpec<Float>
) {
    val animatedTipOffset = animateFloatAsState(tipOffset, animationSpec)
    reset()
    lineTo(x = 0f, y = 0f)
    lineTo(x = (base / 2) + animatedTipOffset.value, y = length)
    lineTo(x = base, y = 0f)
    close()
}

@Composable
private fun Path.TooltipHorizontalPointerPath(
    length: Float,
    base: Float,
    tipOffset: Float,
    animationSpec: AnimationSpec<Float>
) {
    val animatedTipOffset = animateFloatAsState(tipOffset, animationSpec)

    reset()
    lineTo(x = 0f, y = 0f)
    lineTo(x = length, y = (base / 2) + animatedTipOffset.value)
    lineTo(x = 0f, y = base)
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
        val size = if (tooltipSettings.tooltipPointerPosition.isVertical) {
            Size(containerDimens.widthPx, containerDimens.heightPx - tooltipSettings.pointerLengthPx)
        } else {
            Size(containerDimens.widthPx - tooltipSettings.pointerLengthPx, containerDimens.heightPx)
        }

        val offset = when (tooltipSettings.tooltipPointerPosition) {
            is Top -> Offset(0f, tooltipSettings.pointerLengthPx)
            Left -> Offset(tooltipSettings.pointerLengthPx, 0f)
            else -> Offset(0f, 0f)
        }

        addPath(
            Path().apply {
                addOutline(
                    RoundedCornerShape(
                        topStart = cornerTopStart.value,
                        topEnd = cornerTopEnd.value,
                        bottomEnd = cornerBottomEnd.value,
                        bottomStart = cornerBottomStart.value
                    ).createOutline(size, LocalLayoutDirection.current, LocalDensity.current)
                )
            },
            offset
        )
    }
}
