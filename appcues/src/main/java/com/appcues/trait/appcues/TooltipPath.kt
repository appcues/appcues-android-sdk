package com.appcues.trait.appcues

import android.graphics.PointF
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.Left
import com.appcues.trait.appcues.TooltipPointerPosition.Right
import com.appcues.trait.appcues.TooltipPointerPosition.Top
import com.appcues.util.eq
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun drawTooltipPointerPath(
    tooltipSettings: TooltipSettings,
    containerDimens: TooltipContainerDimens?,
    animationFloatSpec: AnimationSpec<Float>,
    animationDpSpec: AnimationSpec<Dp>,
): Path {
    return Path().apply {
        // if containerDimens is null we don't to path the tooltip yet
        if (containerDimens == null) return@apply

        // operation that unifies both the container Path and tooltip pointer Path
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
    val containerCornerRadiusPx = with(density) { containerDimens.cornerRadius.toPx() }

    val (pointerOffsetX, verticalTipOffset) = calculatePointerXOffset(
        containerDimens = containerDimens,
        tooltipSettings = tooltipSettings,
        pointerOffsetX = with(density) { tooltipSettings.pointerOffsetX.value.toPx() },
        containerCornerRadiusPx = containerCornerRadiusPx,
    )

    val (pointerOffsetY, horizontalTipOffset) = calculatePointerYOffset(
        containerDimens = containerDimens,
        tooltipSettings = tooltipSettings,
        pointerOffsetY = with(density) { tooltipSettings.pointerOffsetY.value.toPx() },
        containerCornerRadiusPx = containerCornerRadiusPx,
    )

    val animatedPointerOffsetX = animateFloatAsState(pointerOffsetX, animationSpec)
    val animatedPointerOffsetY = animateFloatAsState(pointerOffsetY, animationSpec)

    // the value of length is the pointerLengthPx considering the direction it should
    val animatedVerticalLength = animateFloatAsState(
        when (tooltipSettings.tooltipPointerPosition) {
            Top -> -tooltipSettings.pointerLengthPx
            Bottom -> tooltipSettings.pointerLengthPx
            else -> 0f
        },
        animationSpec
    )

    val animatedHorizontalLength = animateFloatAsState(
        when (tooltipSettings.tooltipPointerPosition) {
            Left -> -tooltipSettings.pointerLengthPx
            Right -> tooltipSettings.pointerLengthPx
            else -> 0f
        },
        animationSpec
    )

    return Path().apply {
        if (tooltipSettings.tooltipPointerPosition.isVertical) {
            TooltipVerticalPointerPath(tooltipSettings, verticalTipOffset, animatedVerticalLength.value, animationSpec)
            translate(Offset(animatedPointerOffsetX.value, pointerOffsetY))
        } else {
            TooltipHorizontalPointerPath(tooltipSettings, horizontalTipOffset, animatedHorizontalLength.value, animationSpec)
            translate(Offset(pointerOffsetX, animatedPointerOffsetY.value))
        }
    }
}

/**
 * Calculates the pointerXOffset position, returns a pair of value containing:
 * first: pointer x offset
 * second: pointer tip offset
 *
 * during the processing of this function we also update the tooltipPointerPosition horizontal alignment
 */
@SuppressWarnings("MagicNumber")
@Composable
private fun Path.TooltipVerticalPointerPath(
    tooltipSettings: TooltipSettings,
    tipOffset: Float,
    tipLength: Float,
    animationSpec: AnimationSpec<Float>
) {
    val cornerRadius = tooltipSettings.pointerCornerRadiusPx
    val animatedTipOffset = animateFloatAsState(tipOffset, animationSpec)

    // based on point 1,2,3 we define imaginary points 0 and 4 that will help us round the base of the tooltip
    val pt1 = PointF(0f, 0f)
    val pt2 = PointF(tooltipSettings.pointerBaseCenterPx + animatedTipOffset.value, tipLength)
    val pt3 = PointF(tooltipSettings.pointerBasePx, 0f)
    val pt0 = PointF(pt1.x - tooltipSettings.pointerBaseCenterPx, 0f)
    val pt4 = PointF(pt3.x + tooltipSettings.pointerBaseCenterPx, 0f)

    // map points into a list and reverse the order in case vertical tooltip pointer is at bottom
    val points = arrayListOf(pt0, pt1, pt2, pt3, pt4).apply { if (tooltipSettings.tooltipPointerPosition is Bottom) reverse() }

    // from points, define corners to apply cornerRadius
    val corner1 = getTooltipRoundedCorner(points[2], points[1], points[0], cornerRadius, shouldRound = tooltipSettings.isRoundingBase)
    val corner2 = getTooltipRoundedCorner(points[1], points[2], points[3], cornerRadius, clockWise = true)
    val corner3 = getTooltipRoundedCorner(points[4], points[3], points[2], cornerRadius, shouldRound = tooltipSettings.isRoundingBase)

    drawTooltipPath(arrayListOf(corner1, corner2, corner3))
}

@SuppressWarnings("MagicNumber")
@Composable
private fun Path.TooltipHorizontalPointerPath(
    tooltipSettings: TooltipSettings,
    tipOffset: Float,
    tipLength: Float,
    animationSpec: AnimationSpec<Float>
) {
    val cornerRadius = tooltipSettings.pointerCornerRadiusPx
    val animatedTipOffset = animateFloatAsState(tipOffset, animationSpec)

    val pt1 = PointF(0f, tooltipSettings.pointerBasePx)
    val pt2 = PointF(tipLength, tooltipSettings.pointerBaseCenterPx + animatedTipOffset.value)
    val pt3 = PointF(0f, 0f)
    val pt0 = PointF(0f, pt1.y + tooltipSettings.pointerBaseCenterPx)
    val pt4 = PointF(0f, pt3.y - tooltipSettings.pointerBaseCenterPx)

    // map points into a list and reverse the order in case vertical tooltip pointer is at Right
    val points = arrayListOf(pt0, pt1, pt2, pt3, pt4).apply { if (tooltipSettings.tooltipPointerPosition is Right) reverse() }

    // from points, define corners to apply cornerRadius
    val corner1 = getTooltipRoundedCorner(points[2], points[1], points[0], cornerRadius, shouldRound = tooltipSettings.isRoundingBase)
    val corner2 = getTooltipRoundedCorner(points[1], points[2], points[3], cornerRadius, clockWise = true)
    val corner3 = getTooltipRoundedCorner(points[4], points[3], points[2], cornerRadius, shouldRound = tooltipSettings.isRoundingBase)

    drawTooltipPath(arrayListOf(corner1, corner2, corner3))
}

private fun Path.drawTooltipPath(points: List<CornerPoint>) {
    reset()

    points.forEach {
        val startX = it.centerPoint.x + it.radius * cos(it.startAngle)
        val startY = it.centerPoint.y + it.radius * sin(it.startAngle)
        val endX = it.centerPoint.x + it.radius * cos(it.endAngle)
        val endY = it.centerPoint.y + it.radius * sin(it.endAngle)

        val firstSweepAngle = atan2(endY - it.centerPoint.y, endX - it.centerPoint.x) -
            atan2(startY - it.centerPoint.y, startX - it.centerPoint.x)

        val secondSweepAngle = atan2(startX - it.centerPoint.x, startY - it.centerPoint.y) -
            atan2(endX - it.centerPoint.x, endY - it.centerPoint.y)

        // rather than trying to figure out a more advanced way to calculate the sweep angle for
        // our limited case scenarios when rounding the corners of the pointer,
        // performing the following checks ensure the proper value for the sweepAngleDegrees
        val sweepAngle = when {
            firstSweepAngle in -Math.PI..Math.PI -> firstSweepAngle
            firstSweepAngle.eq(secondSweepAngle) -> firstSweepAngle + Math.PI * 2
            else -> secondSweepAngle
        }

        arcTo(
            rect = Rect(it.centerPoint.toOffset(), it.radius),
            startAngleDegrees = Math.toDegrees(it.startAngle.toDouble()).toFloat(),
            sweepAngleDegrees = Math.toDegrees(sweepAngle.toDouble()).toFloat(),
            forceMoveTo = false
        )
    }

    close()
}

private fun PointF.toOffset(): Offset {
    return Offset(x, y)
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
            Top -> Offset(0f, tooltipSettings.pointerLengthPx)
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
