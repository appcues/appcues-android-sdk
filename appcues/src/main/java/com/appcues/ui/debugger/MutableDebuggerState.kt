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

internal class MutableDebuggerState(private val density: Density, private val isCreating: Boolean, val fabSize: Dp = 56.dp) {

    companion object {

        private const val GRID_SCREEN_COUNT = 5
        private const val GRID_FAB_POSITION = 4
        private val EXPANDED_CONTAINER_TOP_PADDING = 24.dp

        private var lastKnownPosition = Offset(0f, 0f)
    }

    val isVisible = MutableTransitionState(isCreating.not())
    val isDragging = MutableTransitionState(false)
    val isExpanded = MutableTransitionState(false)
    val isPaused = mutableStateOf(value = false)

    val fabXOffset = mutableStateOf(value = 0f)
    val fabYOffset = mutableStateOf(value = 0f)

    private var boxSize = IntSize(0, 0)
    private var dismissRect = Rect(Offset(0f, 0f), Size(0f, 0f))
    private var fabRect = Rect(Offset(0f, 0f), Size(0f, 0f))

    fun initFabOffsets(size: IntSize) {
        boxSize = size

        with(density) {
            // if we are creating (meaning the debugger was started) we calculate the initial position,
            // else we just use the last known position we have
            if (isCreating) {
                updatePosition(
                    x = size.width.toFloat() - fabSize.toPx(),
                    y = ((size.height.toFloat() / GRID_SCREEN_COUNT) * GRID_FAB_POSITION) - fabSize.toPx()
                )
            } else {
                updatePosition(
                    x = lastKnownPosition.x,
                    y = lastKnownPosition.y
                )
            }
        }
    }

    fun getFabPositionAsIntOffset(): IntOffset {
        return IntOffset(fabXOffset.value.roundToInt(), fabYOffset.value.roundToInt())
    }

    fun updateFabOffsets(dragAmount: Offset) {
        with(density) {
            updatePosition(
                x = (fabXOffset.value + dragAmount.x).coerceIn(0f, boxSize.width.toFloat() - fabSize.toPx()),
                y = (fabYOffset.value + dragAmount.y).coerceIn(0f, boxSize.height.toFloat() - fabSize.toPx())
            )
        }
    }

    private fun updatePosition(x: Float, y: Float) {
        fabXOffset.value = x
        fabYOffset.value = y
        updateFabRect(x, y)

        // update global offset value with latest update
        lastKnownPosition = Offset(x, y)
    }

    private fun updateFabRect(x: Float, y: Float) {
        with(density) {
            fabRect = Rect(
                offset = Offset(x, y),
                size = Size(fabSize.toPx(), fabSize.toPx())
            )
        }
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

    fun getExpandedContainerHeight(): Dp {
        return with(density) {
            boxSize.height.toDp() - (fabSize / 2) - EXPANDED_CONTAINER_TOP_PADDING
        }
    }

    fun getExpandedFabAnchor(): Offset {
        return with(density) {
            Offset((boxSize.width - fabSize.toPx()) / 2, EXPANDED_CONTAINER_TOP_PADDING.toPx())
        }
    }

    fun getLastIdleFabAnchor(): Offset {
        return fabRect.topLeft
    }

    fun shouldAnimateToIdle(): Boolean {
        return fabRect.topLeft.let {
            it.x != fabXOffset.value && it.y != fabYOffset.value
        }
    }
}
