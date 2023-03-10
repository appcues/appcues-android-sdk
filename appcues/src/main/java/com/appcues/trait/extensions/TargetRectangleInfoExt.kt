package com.appcues.trait.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.trait.appcues.ContentPreferredPosition
import com.appcues.trait.appcues.TARGET_RECTANGLE_METADATA
import com.appcues.trait.appcues.TargetRectangleInfo
import com.appcues.trait.appcues.TooltipContainerDimens
import com.appcues.trait.appcues.TooltipPointerPosition
import com.appcues.trait.appcues.TooltipTrait
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.utils.AppcuesWindowInfo

@Composable
internal fun rememberTargetRectangleInfo(metadata: AppcuesStepMetadata): TargetRectangleInfo? {
    return metadata.actual[TARGET_RECTANGLE_METADATA] as TargetRectangleInfo?
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
    containerDimens: TooltipContainerDimens?,
    targetRect: Rect?,
): TooltipPointerPosition {
    if (targetRect == null || containerDimens == null) return TooltipPointerPosition.None

    val topSafeArea = targetRect.top.dp - TooltipTrait.SCREEN_VERTICAL_PADDING
    val bottomSafeArea = windowInfo.heightDp - TooltipTrait.SCREEN_VERTICAL_PADDING - targetRect.bottom.dp

    return when (this?.prefPosition) {
        ContentPreferredPosition.TOP ->
            if (topSafeArea > containerDimens.heightDp) TooltipPointerPosition.Bottom else TooltipPointerPosition.Top
        ContentPreferredPosition.BOTTOM ->
            if (bottomSafeArea > containerDimens.heightDp) TooltipPointerPosition.Top else TooltipPointerPosition.Bottom
        else -> when {
            targetRect.center.y.dp < windowInfo.heightDp / 2 -> TooltipPointerPosition.Top
            else -> TooltipPointerPosition.Bottom
        }
    }
}
