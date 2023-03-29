package com.appcues.ui.extensions

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import java.lang.Float.max

/**
 * Defines a path to draw the X by drawing two lines crossing from edge to edge.
 */
internal fun DrawScope.xShapePath(pathSize: Dp): Path {
    return Path()
        .apply {
            val pathSizePx = pathSize.toPx()
            val sizeRect = size.toRect()

            // center the desired size within the current bounds
            // find the delta to deflate the container rect in each direction
            val deltaX = max(size.width - pathSizePx, 0.0f) / 2
            val deltaY = max(size.height - pathSizePx, 0.0f) / 2

            val minX = 0 + deltaX
            val minY = 0 + deltaY
            val maxX = sizeRect.right - deltaX
            val maxY = sizeRect.bottom - deltaY

            moveTo(minX, minY) // move to top left
            lineTo(maxX, maxY) // line to bottom right
            moveTo(minX, maxY) // move to bottom left
            lineTo(maxX, minY) // line to top right
        }
}
