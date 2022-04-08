package com.appcues.ui.debugger

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal class MutableDebuggerState(private val density: Density, val fabSize: Dp = 56.dp) {

    companion object {

        private const val GRID_SCREEN_COUNT = 5
        private const val GRID_FAB_POSITION = 4
    }

    val isVisible = MutableTransitionState(false)
    val isDragging = MutableTransitionState(false)
    val fabXOffset = mutableStateOf(value = 0f)
    val fabYOffset = mutableStateOf(value = 0f)

    private var boxSize = IntSize(0, 0)
    private var dismissRect = Rect(Offset(0f, 0f), Size(0f, 0f))
    private var fabRect = Rect(Offset(0f, 0f), Size(0f, 0f))

    fun initFabOffsets(size: IntSize) {
        boxSize = size

        with(density) {
            fabXOffset.value = size.width.toFloat() - fabSize.toPx()
            fabYOffset.value = ((size.height.toFloat() / GRID_SCREEN_COUNT) * GRID_FAB_POSITION) - fabSize.toPx()

            updateFabRect()
        }
    }

    fun getFabPositionAsIntOffset(): IntOffset {
        return IntOffset(fabXOffset.value.roundToInt(), fabYOffset.value.roundToInt())
    }

    fun updateFabOffsets(dragAmount: Offset) {
        with(density) {
            fabXOffset.value = (fabXOffset.value + dragAmount.x)
                .coerceIn(0f, boxSize.width.toFloat() - fabSize.toPx())

            fabYOffset.value = (fabYOffset.value + dragAmount.y)
                .coerceIn(0f, boxSize.height.toFloat() - fabSize.toPx())

            updateFabRect()
        }
    }

    private fun Density.updateFabRect() {
        fabRect = Rect(
            offset = Offset(fabXOffset.value, fabYOffset.value),
            size = Size(fabSize.toPx(), fabSize.toPx())
        )
    }

    fun initDismissAreaRect(layoutCoordinates: LayoutCoordinates) {
        with(density) {
            dismissRect = Rect(
                offset = layoutCoordinates.positionInRoot(),
                size = Size(layoutCoordinates.size.width.toFloat(), layoutCoordinates.size.height.toFloat())
            ).run {
                // reduces the rectangle size so we need to drop the
                // fab closer to the center to dismiss it
                deflate(28.dp.toPx())
            }
        }
    }

    fun isFabInDismissingArea(): Boolean {
        return dismissRect.overlaps(fabRect)
    }

    fun getDismissAreaTargetXOffset(): Float {
        return dismissRect.center.x - fabRect.size.width / 2
    }

    fun getDismissAreaTargetYOffset(): Float {
        return dismissRect.center.y - fabRect.size.height / 2
    }
}
