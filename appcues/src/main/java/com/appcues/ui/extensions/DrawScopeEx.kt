package com.appcues.ui.extensions

import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines a path to draw the X by drawing two lines crossing from edge to edge.
 */
internal fun DrawScope.xShapePath(color: Color, deflateDp: Dp = 0.dp): Path {
    return Path()
        .apply {
            val strokeWidth = 2.dp.toPx()
            val deflate = deflateDp.toPx()
            val sizeRect = size
                .toRect()
                .deflate(deflate)

            drawLine(
                color = color,
                start = sizeRect.topLeft,
                end = sizeRect.bottomRight,
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = color,
                start = sizeRect.bottomLeft,
                end = sizeRect.topRight,
                strokeWidth = strokeWidth,
            )
        }
}
