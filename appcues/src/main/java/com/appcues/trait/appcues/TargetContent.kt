package com.appcues.trait.appcues

internal const val TARGET_RECTANGLE_METADATA = "targetRectangle"

internal data class TargetRectangleInfo(
    val x: Float = 0f,
    val y: Float = 0f,
    val relativeX: Double = 0.0,
    val relativeY: Double = 0.0,
    val width: Float = 0f,
    val height: Float = 0f,
    val relativeWidth: Double = 0.0,
    val relativeHeight: Double = 0.0,
    val contentDistance: Double = 0.0,
    val prefPosition: ContentPreferredPosition? = null
)

internal enum class ContentPreferredPosition {
    TOP, BOTTOM, LEFT, RIGHT
}

internal fun String?.toPosition(): ContentPreferredPosition? {
    return when (this) {
        "top" -> ContentPreferredPosition.TOP
        "bottom" -> ContentPreferredPosition.BOTTOM
        "left" -> ContentPreferredPosition.LEFT
        "right" -> ContentPreferredPosition.RIGHT
        else -> null
    }
}
