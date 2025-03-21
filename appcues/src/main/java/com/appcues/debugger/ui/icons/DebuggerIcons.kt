package com.appcues.debugger.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.DefaultFillType
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Similar solution for icons used in Material-Icons-Extensions library
 */
internal object DebuggerIcons {

    object Filled

    object Outlined
}

/**
 * Utility delegate to construct an Icon with default size information.
 * This is used by generated icons, and should not be used manually.
 *
 * @param name the full name of the generated icon
 * @param autoMirror determines if the vector asset should automatically be mirrored for right to
 * left locales
 * @param block builder lambda to add paths to this vector asset
 */
internal inline fun debuggerIcon(
    name: String,
    autoMirror: Boolean = false,
    block: ImageVector.Builder.() -> ImageVector.Builder
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24F,
    viewportHeight = 24F,
    autoMirror = autoMirror
).block().build()

/**
 * Adds a vector path to this icon with Material defaults.
 *
 * @param fillAlpha fill alpha for this path
 * @param strokeAlpha stroke alpha for this path
 * @param pathFillType [PathFillType] for this path
 * @param pathBuilder builder lambda to add commands to this path
 */
internal inline fun ImageVector.Builder.iconPath(
    fillAlpha: Float = 1f,
    strokeAlpha: Float = 1f,
    pathFillType: PathFillType = DefaultFillType,
    pathBuilder: PathBuilder.() -> Unit
) =
    path(
        fill = SolidColor(Color.Black),
        fillAlpha = fillAlpha,
        stroke = null,
        strokeAlpha = strokeAlpha,
        strokeLineWidth = 1f,
        strokeLineCap = StrokeCap.Butt,
        strokeLineJoin = StrokeJoin.Bevel,
        strokeLineMiter = 1f,
        pathFillType = pathFillType,
        pathBuilder = pathBuilder
    )
