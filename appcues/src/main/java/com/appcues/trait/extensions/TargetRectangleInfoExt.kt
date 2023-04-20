package com.appcues.trait.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
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

    val screenWidth = windowInfo.widthDp.value
    val screenHeight = windowInfo.heightDp.value

    return Rect(
        offset = Offset(
            x = (screenWidth * relativeX).toFloat() + x,
            y = (screenHeight * relativeY).toFloat() + y,
        ),
        size = Size(
            width = (screenWidth * relativeWidth).toFloat() + width,
            height = (screenHeight * relativeHeight).toFloat() + height,
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

    val availableSpaceTop = targetRect.top.dp - TooltipTrait.SCREEN_VERTICAL_PADDING
    val availableSpaceBottom = windowInfo.heightDp - TooltipTrait.SCREEN_VERTICAL_PADDING - targetRect.bottom.dp

    val excessSpaceTop = availableSpaceTop - contentDimens.heightDp - distance - pointerLength
    val excessSpaceBottom = availableSpaceBottom - contentDimens.heightDp - distance - pointerLength

    val availableSpaceLeft = targetRect.left.dp - TooltipTrait.SCREEN_HORIZONTAL_PADDING
    val availableSpaceRight = windowInfo.widthDp - TooltipTrait.SCREEN_HORIZONTAL_PADDING

    val excessSpaceLeft = availableSpaceLeft - contentDimens.widthDp - distance - pointerLength
    val excessSpaceRight = availableSpaceRight - targetRect.right.dp - contentDimens.widthDp - distance - pointerLength

    val canPositionVertically = excessSpaceTop > 0.dp || excessSpaceBottom > 0.dp
    val canPositionHorizontally = excessSpaceLeft > 0.dp || excessSpaceRight > 0.dp

    return prefPosition.toPointerPosition(excessSpaceTop, excessSpaceBottom, excessSpaceLeft, excessSpaceRight) ?: when {
        // passed the preference positions we position the tooltip wherever is available.
        canPositionVertically -> if (excessSpaceTop > excessSpaceBottom) Bottom else Top
        canPositionHorizontally -> if (excessSpaceLeft > excessSpaceRight) Right else Left
        // Doesn't fit anywhere so pick the top/bottom side that has the most space.
        // Allowing left/right here would mean the width gets compressed and that opens a can of worms.
        excessSpaceTop > excessSpaceBottom -> Bottom
        else -> Top
    }
}

private fun ContentPreferredPosition?.toPointerPosition(
    excessSpaceTop: Dp,
    excessSpaceBottom: Dp,
    excessSpaceLeft: Dp,
    excessSpaceRight: Dp
): TooltipPointerPosition? {
    return when {
        this == TOP && excessSpaceTop > 0.dp -> Bottom
        this == BOTTOM && excessSpaceBottom > 0.dp -> Top
        this == LEFT && excessSpaceLeft > 0.dp -> Right
        this == RIGHT && excessSpaceRight > 0.dp -> Left
        else -> null
    }
}
