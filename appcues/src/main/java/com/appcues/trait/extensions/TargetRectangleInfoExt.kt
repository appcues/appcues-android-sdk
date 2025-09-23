package com.appcues.trait.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.appcues.trait.appcues.ContentPreferredPosition
import com.appcues.trait.appcues.ContentPreferredPosition.BOTTOM
import com.appcues.trait.appcues.ContentPreferredPosition.LEFT
import com.appcues.trait.appcues.ContentPreferredPosition.RIGHT
import com.appcues.trait.appcues.ContentPreferredPosition.TOP
import com.appcues.trait.appcues.TARGET_RECTANGLE_METADATA
import com.appcues.trait.appcues.TargetRectangleInfo
import com.appcues.trait.appcues.TooltipContentDimens
import com.appcues.trait.appcues.TooltipPointerPosition
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.Left
import com.appcues.trait.appcues.TooltipPointerPosition.None
import com.appcues.trait.appcues.TooltipPointerPosition.Right
import com.appcues.trait.appcues.TooltipPointerPosition.Top
import com.appcues.trait.appcues.TooltipTrait
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.utils.AppcuesWindowInfo

@Composable
internal fun rememberTargetRectangleInfo(metadata: AppcuesStepMetadata): TargetRectangleInfo? {
    return metadata.current[TARGET_RECTANGLE_METADATA] as TargetRectangleInfo?
}

internal fun TargetRectangleInfo?.getRect(windowInfo: AppcuesWindowInfo): Rect? {
    if (this == null) return null

    val safeRect = windowInfo.safeRect
    val availableScreenWidth = safeRect.width
    val availableScreenHeight = safeRect.height

    return Rect(
        offset = Offset(
            x = (availableScreenWidth * relativeX).toFloat() + x + safeRect.left,
            y = (availableScreenHeight * relativeY).toFloat() + y + safeRect.top,
        ),
        size = Size(
            width = (availableScreenWidth * relativeWidth).toFloat() + width,
            height = (availableScreenHeight * relativeHeight).toFloat() + height,
        )
    )
}

internal fun TargetRectangleInfo?.getContentDistance(): Dp {
    return this?.contentDistance?.dp ?: 0.0.dp
}

internal fun TargetRectangleInfo?.getTooltipPointerPosition(
    windowInfo: AppcuesWindowInfo,
    contentDimens: TooltipContentDimens?,
    targetRect: Rect?,
    distance: Dp,
    pointerLength: Dp,
): TooltipPointerPosition {
    if (this == null || targetRect == null || contentDimens == null) return None

    val safeRect = windowInfo.safeRect
    val availableScreenHeight = safeRect.height.dp
    val availableScreenWidth = safeRect.width.dp

    val availableSpaceTop = targetRect.top.dp - TooltipTrait.SCREEN_VERTICAL_PADDING - safeRect.top.dp
    val availableSpaceBottom = availableScreenHeight - targetRect.bottom.dp - TooltipTrait.SCREEN_VERTICAL_PADDING

    val excessSpaceTop = availableSpaceTop - contentDimens.heightDp - distance - pointerLength
    val excessSpaceBottom = availableSpaceBottom - contentDimens.heightDp - distance - pointerLength

    val availableSpaceLeft = targetRect.left.dp - TooltipTrait.SCREEN_HORIZONTAL_PADDING - safeRect.left.dp
    val availableSpaceRight = availableScreenWidth - targetRect.right.dp - TooltipTrait.SCREEN_HORIZONTAL_PADDING

    val excessSpaceLeft = availableSpaceLeft - contentDimens.widthDp - distance - pointerLength
    val excessSpaceRight = availableSpaceRight - contentDimens.widthDp - distance - pointerLength

    val canPositionVertically = excessSpaceTop > 0.dp || excessSpaceBottom > 0.dp
    val canPositionHorizontally = excessSpaceLeft > 0.dp || excessSpaceRight > 0.dp

    // figures out the constraints of the screen, and calculate availableHeight for top and bottom, in case we need to pass it
    // as part of the TooltipPointerPosition
    val minHeight = 48.dp + pointerLength
    val maxHeight = availableScreenHeight - (TooltipTrait.SCREEN_VERTICAL_PADDING * 2)
    val availableHeightTop = (availableSpaceTop - distance - pointerLength).coerceIn(minHeight, maxHeight)
    val availableHeightBottom = (availableSpaceBottom - distance - pointerLength).coerceIn(minHeight, maxHeight)

    return prefPosition.toPointerPosition(excessSpaceTop, excessSpaceBottom, excessSpaceLeft, excessSpaceRight) ?: when {
        // passed the preference positions we position the tooltip wherever is available.
        canPositionVertically -> if (excessSpaceTop > excessSpaceBottom) Bottom() else Top()
        canPositionHorizontally -> if (excessSpaceLeft > excessSpaceRight) Right else Left
        // Doesn't fit anywhere so pick the top/bottom side that has the most space.
        // Allowing left/right here would mean the width gets compressed and that opens a can of worms.
        excessSpaceTop > excessSpaceBottom -> Bottom(availableHeightTop)
        else -> Top(availableHeightBottom)
    }
}

private fun ContentPreferredPosition?.toPointerPosition(
    excessSpaceTop: Dp,
    excessSpaceBottom: Dp,
    excessSpaceLeft: Dp,
    excessSpaceRight: Dp
): TooltipPointerPosition? {
    return when {
        this == TOP && excessSpaceTop > 0.dp -> Bottom()
        this == BOTTOM && excessSpaceBottom > 0.dp -> Top()
        this == LEFT && excessSpaceLeft > 0.dp -> Right
        this == RIGHT && excessSpaceRight > 0.dp -> Left
        else -> null
    }
}
