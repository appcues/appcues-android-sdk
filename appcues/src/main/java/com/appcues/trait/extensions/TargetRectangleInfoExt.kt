package com.appcues.trait.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.trait.appcues.TargetRectangleTrait
import com.appcues.trait.appcues.TargetRectangleTrait.TargetRectangleInfo
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.utils.AppcuesWindowInfo

@Composable
internal fun rememberTargetRectangleInfo(metadata: AppcuesStepMetadata): TargetRectangleInfo? {
    return metadata.actual[TargetRectangleTrait.TARGET_RECTANGLE_METADATA] as TargetRectangleInfo?
}

@Composable
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

@Composable
internal fun TargetRectangleInfo?.getContentDistance(): Dp {
    return this?.contentDistance?.dp ?: 0.0.dp
}
