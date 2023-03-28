package com.appcues.ui.extensions

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import java.lang.Float.max

/**
 * Defines a path to draw the X by drawing two lines crossing from edge to edge.
 */
internal fun DrawScope.xShapePath(
    color: Color,
    pathSize: Dp,
    strokeWidth: Dp,
): Path {
    return Path()
        .apply {
            val strokeWidthPx = strokeWidth.toPx()
            val pathSizePx = pathSize.toPx()

            // center the desired size within the current bounds
            val deltaX = max(size.width - pathSizePx, 0.0f) / 2
            val deltaY = max(size.height - pathSizePx, 0.0f) / 2

            val sizeRect = size.toRect()
            val pathRect = Rect(
                left = sizeRect.left + deltaX,
                top = sizeRect.top + deltaY,
                right = sizeRect.right - deltaX,
                bottom = sizeRect.bottom - deltaY
            )

            drawLine(
                color = color,
                start = pathRect.topLeft,
                end = pathRect.bottomRight,
                strokeWidth = strokeWidthPx,
            )
            drawLine(
                color = color,
                start = pathRect.bottomLeft,
                end = pathRect.topRight,
                strokeWidth = strokeWidthPx,
            )
        }
}
