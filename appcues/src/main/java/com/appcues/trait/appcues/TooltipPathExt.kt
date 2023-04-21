package com.appcues.trait.appcues

import android.graphics.PointF
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.Left
import com.appcues.trait.appcues.TooltipPointerPosition.Right
import com.appcues.trait.appcues.TooltipPointerPosition.Top
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.tan

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

    /**
     * returns ContainerCornerRadius that based on TooltipPointerPosition, will ignore/use
     * the existing cornerRadius value for some of the edges.
     *
     * eg. When HorizontalAlignment is Left the container topStart should not apply cornerRadius.
     */
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
    val tooltipPointerPosition: TooltipPointerPosition,
    val distance: Dp,
    val pointerBasePx: Float,
    val pointerLengthPx: Float,
    val pointerCornerRadiusPx: Float,
    val isRoundingBase: Boolean,
) {

    val pointerBaseCenterPx = pointerBasePx / 2
    val pointerOffsetX = mutableStateOf(0.dp)
    val pointerOffsetY = mutableStateOf(0.dp)
}

// represents the size of the content only, not including the pointer
internal data class TooltipContentDimens(
    val widthDp: Dp,
    val heightDp: Dp,
)

// represents the full size of the composition, including the pointer
internal data class TooltipContainerDimens(
    val widthDp: Dp,
    val heightDp: Dp,
    val widthPx: Float,
    val heightPx: Float,
    val cornerRadius: Dp,
) {

    val availableHorizontalSpace = (widthDp - (cornerRadius * 2)).coerceAtLeast(0.dp)
    val availableVerticalSpace = (heightDp - (cornerRadius * 2)).coerceAtLeast(0.dp)
}

internal fun calculatePointerXOffset(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    pointerOffsetX: Float,
    containerCornerRadiusPx: Float,
): Pair<Float, Float> {
    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + containerCornerRadiusPx
    val maxPointerOffset = max((containerDimens.widthPx - tooltipSettings.pointerBasePx), 0f)
    val maxPointerOffsetCornerRadius = maxPointerOffset - containerCornerRadiusPx

    // figure out the offset of the pointer and offset for the tooltip pointer when hits the edges
    var pointerTipOffset = 0f
    var pointerOffset = (containerDimens.widthPx / 2) - tooltipSettings.pointerBaseCenterPx + pointerOffsetX

    when {
        // * offset the pointer to the right of the content
        tooltipSettings.tooltipPointerPosition is Right -> {
            pointerOffset = containerDimens.widthPx - tooltipSettings.pointerLengthPx
        }
        // * offset the pointer to the left of the content
        tooltipSettings.tooltipPointerPosition is Left -> {
            pointerOffset = tooltipSettings.pointerLengthPx
        }
        // After this it means the position is either TOP or BOTTOM. - NONE is irrelevant here since the pointer is not drawn.
        // * pointer offset is between min and max, we coerceIn the value accounting for cornerRadius and set alignment to CENTER
        pointerOffset >= minPointerOffset && pointerOffset <= maxPointerOffset -> {
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.CENTER)
            pointerOffset = pointerOffset.coerceIn(minPointerOffsetCornerRadius..maxPointerOffsetCornerRadius).toFloat()
        }
        // * pointer is less than the min, its anchored to the LEFT
        pointerOffset < minPointerOffset -> {
            pointerTipOffset = -tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.LEFT)
            pointerOffset = minPointerOffset
        }
        // * pointer is greater than the max, its anchored to the RIGHT
        pointerOffset > maxPointerOffset -> {
            pointerTipOffset = tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.horizontalAlignment(PointerHorizontalAlignment.RIGHT)
            pointerOffset = maxPointerOffset
        }
    }

    return Pair(pointerOffset, pointerTipOffset)
}

/**
 * Calculates the pointerYOffset position, returns a pair of value containing:
 * first: pointer y offset
 * second: pointer tip offset
 *
 * during the processing of this function we also update the tooltipPointerPosition vertical alignment
 */
internal fun calculatePointerYOffset(
    containerDimens: TooltipContainerDimens,
    tooltipSettings: TooltipSettings,
    pointerOffsetY: Float,
    containerCornerRadiusPx: Float
): Pair<Float, Float> {
    // boundaries for the tooltip
    val minPointerOffset = 0f
    val minPointerOffsetCornerRadius = minPointerOffset + containerCornerRadiusPx
    val maxPointerOffset = max(containerDimens.heightPx - tooltipSettings.pointerBasePx, 0f)
    val maxPointerOffsetCornerRadius = max(maxPointerOffset - containerCornerRadiusPx, minPointerOffsetCornerRadius)

    // figure out the offset of the pointer and offset for the tooltip pointer when hits the edges
    var pointerTipOffset = 0f
    var pointerOffset = (containerDimens.heightPx / 2) - tooltipSettings.pointerBaseCenterPx + pointerOffsetY

    when {
        // * offset the pointer to the bottom of the content
        tooltipSettings.tooltipPointerPosition is Bottom -> {
            pointerOffset = containerDimens.heightPx - tooltipSettings.pointerLengthPx
        }
        // * offset the pointer to the top of the content
        tooltipSettings.tooltipPointerPosition is Top -> {
            pointerOffset = tooltipSettings.pointerLengthPx
        }
        // After this it means the position is either LEFT or RIGHT. - NONE is irrelevant here since the pointer is not drawn.
        // * pointer offset is between min and max, we coerceIn the value accounting for cornerRadius and set alignment to CENTER
        pointerOffset >= minPointerOffset && pointerOffset <= maxPointerOffset -> {
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.CENTER)
            pointerOffset = pointerOffset.coerceIn(minPointerOffsetCornerRadius..maxPointerOffsetCornerRadius).toFloat()
        }
        // * pointer is less or equal to the min, its anchored to the TOP
        pointerOffset < minPointerOffset -> {
            pointerTipOffset = -tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.TOP)
            pointerOffset = minPointerOffset
        }
        // * pointer is greater or equal the max, its anchored to the BOTTOM
        pointerOffset > maxPointerOffset -> {
            pointerTipOffset = tooltipSettings.pointerBaseCenterPx
            tooltipSettings.tooltipPointerPosition.verticalAlignment(PointerVerticalAlignment.BOTTOM)
            pointerOffset = maxPointerOffset
        }
    }

    return Pair(pointerOffset, pointerTipOffset)
}

/**
 * How does this work?
 * We're interested in the radius value such that the arc from outer circle (C1) and arc from the inner circle (C2)
 * intersect, drawing a continuous line for the pointer rather than C2 exceeding C1 and there being an odd straight
 * line while the path backtracks.
 *
 * There's a few constraints we know:
 * 1. The angle of the line between C1 and C2 (perpendicular to the side of the pointer triangle)
 * 2. The x value of the center of C2 must be in the middle of the pointer (so pointerSize.width / 2)
 * 3. The y value of the center of C2 (h) can be calculated from the tip of the pointer
 * 4. The distance between C1 and C2 is 2 * radius
 * 5. The center of C1 can be calculated by projecting from C2 distance 2 * radius by the angle
 * 6. The y value of the center of C1 must be exactly -radius from the base (so pointerSize.height - radius)
 */
internal fun calculateTooltipMaxRadius(base: Float, height: Float): Float {
    // pt1 and pt2 represents a rectangular triangle based on the original shape
    val pt1 = PointF(0f, height)
    val pt2 = PointF(base / 2f, 0f)
    // the angle from pt1 and pt2 is the angle that is perpendicular to one side of the said shape.
    val angle = atan2(pt1.y, pt2.x) // (1)*
    // h = r / cos(angle)                                // (3)*
    //
    // centerOfC2 = Point(
    //    x: pt2.x,                                      // (2)
    //    y: pt2.y + h                                   // (3)*
    // )
    //
    // To project C1 (Outer circle based on known C2)
    // centerOfC1 = Point(
    //    x: centerC2.x - cos(angle - .pi / 2) * r * 2,  // (4, 5)
    //    y: centerC2.y + sin(angle - .pi / 2) * r * 2   // (4, 5)*
    // )
    //
    // We know that pt1.y - r = centerC1.y               // (6)*
    //
    // Substitute and solve r (radius) for:
    // r = pt1.y - centerC1.y
    // r = (pt1.y) - (centerC2.y + sin(angle - .pi / 2) * r * 2)
    // r = (pt1.y) - ((pt2.y + h) + sin(angle - Math.PI / 2) * r * 2)
    // r = (pt1.y) - (pt2.x) - (r/cos(angle)) - sin(angle - Math.PI / 2) * r * 2
    // r + (r/cos(angle)) + sin(angle - Math.PI / 2) * r * 2 = (pt1.y) - (pt2.x)
    // r * (1 + (1/cos(angle)) + sin(angle - Math.PI / 2) * 2) = (pt1.y) - (pt2.x)
    // r = ((pt1.y) - (pt2.x)) / (1 + (1/cos(angle)) + sin(angle - Math.PI / 2) * 2)
    return (pt1.y - pt2.y) / (1 + 2 * sin(angle - Math.PI / 2) + (1 / cos(angle))).toFloat()
}

/**
 * determine the real width of the pointer tooltip
 */
// the value calculated here should be factored when figuring out the amount of cornerRadius to be applied,
// if any
internal fun calculateTooltipWidth(base: Float, length: Float, radius: Float): Float {
    // (1) figure out the angle
    val angle = atan2(base, length / 2) / 2
    // (2) finds out the value of the opposite side using:
    //        - tan(angle) = opposite side / adjacent side
    //
    //     giving that the adjacent side is radius, then:
    //        - opposite side = tan(angle) * radius
    //
    // (3) multiply by two to account for the two sides of the pointer
    //     and add the existing width to the end result
    return (tan(angle) * radius * 2) + base
}

internal data class CornerPoint(
    val centerPoint: PointF,
    val startAngle: Float,
    val endAngle: Float,
    val radius: Float,
    val isClockWise: Boolean,
)

internal fun getTooltipRoundedCorner(
    from: PointF,
    via: PointF,
    to: PointF,
    radius: Float,
    clockWise: Boolean = false,
    shouldRound: Boolean = true,
): CornerPoint {
    val startAngle = (atan2(via.y - from.y, via.x - from.x) - Math.PI / 2).toFloat()
    val endAngle = (atan2(to.y - via.y, to.x - via.x) - Math.PI / 2).toFloat()

    return CornerPoint(
        centerPoint = if (shouldRound) findRadiusCenterPoint(from, via, to, radius) else via,
        startAngle = if (clockWise) startAngle else endAngle,
        endAngle = if (clockWise) endAngle else startAngle,
        radius = if (shouldRound) radius else 0f,
        isClockWise = clockWise
    )
}

/**
 * Finds the center point for the given edge of the shape, it works by figuring out where the projected lines
 * intersect in 2D space.
 */
private fun findRadiusCenterPoint(
    from: PointF,
    via: PointF,
    to: PointF,
    radius: Float
): PointF {
    val fromAngle = atan2(via.y - from.y, via.x - from.x)
    val toAngle = atan2(to.y - via.y, to.x - via.x)
    val fromOffset = Offset(-sin(fromAngle) * radius, cos(fromAngle) * radius)
    val toOffset = Offset(-sin(toAngle) * radius, cos(toAngle) * radius)

    // projects lines for given radius
    val line1pt1x = from.x + fromOffset.x
    val line1pt1y = from.y + fromOffset.y
    val line1pt2x = via.x + fromOffset.x
    val line1pt2y = via.y + fromOffset.y

    val line2pt1x = via.x + toOffset.x
    val line2pt1y = via.y + toOffset.y
    val line2pt2x = to.x + toOffset.x
    val line2pt2y = to.y + toOffset.y

    // figure out x, y intersection point
    val intersectionX = (
        (line1pt1x * line1pt2y - line1pt1y * line1pt2x) *
            (line2pt1x - line2pt2x) - (line1pt1x - line1pt2x) *
            (line2pt1x * line2pt2y - line2pt1y * line2pt2x)
        ) / (
        (line1pt1x - line1pt2x) * (line2pt1y - line2pt2y) -
            (line1pt1y - line1pt2y) * (line2pt1x - line2pt2x)
        )

    val intersectionY = (
        (line1pt1x * line1pt2y - line1pt1y * line1pt2x) *
            (line2pt1y - line2pt2y) - (line1pt1y - line1pt2y) *
            (line2pt1x * line2pt2y - line2pt1y * line2pt2x)
        ) / (
        (line1pt1x - line1pt2x) * (line2pt1y - line2pt2y) -
            (line1pt1y - line1pt2y) * (line2pt1x - line2pt2x)
        )

    return if (intersectionX.isNaN() || intersectionY.isNaN()) via else PointF(intersectionX, intersectionY)
}
