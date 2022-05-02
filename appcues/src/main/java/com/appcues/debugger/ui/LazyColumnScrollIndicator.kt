package com.appcues.debugger.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged

private const val SCROLL_ON_ALPHA_DELAY = 150
private const val SCROLL_OFF_ALPHA_DELAY = 500

internal fun Modifier.lazyColumnScrollIndicator(state: LazyListState): Modifier {
    return composed(
        factory = {
            val scrollIndicatorState = remember { VerticalScrollIndicatorState() }
            val indicatorSize = scrollIndicatorState.indicatorSize.value
            val scrollOffset = scrollIndicatorState.scrollOffset.value

            LaunchedEffect(state) {
                snapshotFlow { state.layoutInfo to state.firstVisibleItemScrollOffset }
                    .distinctUntilChanged()
                    .collect {
                        scrollIndicatorState.onLazyListInfoChanged(it.first)
                        scrollIndicatorState.updateScrollAmount(it.second)
                    }
            }

            val alpha by animateFloatAsState(
                targetValue = if (state.isScrollInProgress) 1f else 0f,
                animationSpec = tween(durationMillis = if (state.isScrollInProgress) SCROLL_ON_ALPHA_DELAY else SCROLL_OFF_ALPHA_DELAY)
            )

            val animatedYOffset = animateFloatAsState(targetValue = scrollOffset.toFloat())
            val animatedSize = animateFloatAsState(targetValue = indicatorSize.toFloat(), animationSpec = tween(durationMillis = 1000))
            drawForeground {
                if ((state.isScrollInProgress || alpha > 0.0f) && indicatorSize != 0) {
                    drawRect(
                        color = Color(color = 0xFFB0B6CF),
                        topLeft = Offset(this.size.width - 4.dp.toPx(), animatedYOffset.value),
                        size = Size(4.dp.toPx(), animatedSize.value),
                        alpha = alpha,
                    )
                }
            }
        }
    )
}

private class VerticalScrollIndicatorState {

    private var lastItemOffset = 0
    private var viewportHeight = 0
    private var firstItemHeight = 0
    private var lastItemHeight = 0
    private var totalItemsCount = 0
    private var visibleItemsRange = IntRange(0, 0)
    private var lastProcessedScrollAmount = 0
    private var lastItemsHeightAtTop = 0
    private var isLastItemShowing = false
    private var isFirstItemShowing = false

    val scrollOffset = mutableStateOf(0)
    val indicatorSize = mutableStateOf(0)

    fun updateScrollAmount(scrollAmount: Int) {
        // this is where we do a trick calculation to determine how much we want to move the
        // indicator. its not perfect but its working right now but we could revisit this later
        // if the result is not satisfying.
        isLastItemShowing = getItemsHeightAtBottom() == 0
        isFirstItemShowing = getItemsHeightAtTop() == 0

        scrollOffset.value = if (isLastItemShowing) {
            // when we see the last item we get the remaining scroll space and use it as base to determine
            // how much we will move the indicator
            val remainingScroll = viewportHeight - indicatorSize.value - lastItemsHeightAtTop - lastProcessedScrollAmount
            val lastItemScrollAmount = viewportHeight - lastItemOffset
            val bottomItemScrollAmount = (remainingScroll * lastItemScrollAmount) / lastItemHeight

            lastItemsHeightAtTop + lastProcessedScrollAmount + bottomItemScrollAmount
        } else {
            // on all other cases we just get the scroll amount and convert to what would be
            // one item height in scroll indicator measurement
            lastItemsHeightAtTop = getItemsHeightAtTop()
            lastProcessedScrollAmount = (getItemHeight() * scrollAmount) / firstItemHeight
            lastItemsHeightAtTop + lastProcessedScrollAmount
        }
    }

    fun onLazyListInfoChanged(info: LazyListLayoutInfo) {
        // store updated values first before calculating anything
        firstItemHeight = info.visibleItemsInfo.first().size
        lastItemHeight = info.visibleItemsInfo.last().size
        lastItemOffset = info.visibleItemsInfo.last().offset
        totalItemsCount = info.totalItemsCount
        viewportHeight = info.viewportSize.height

        if (info.visibleItemsInfo.isNotEmpty()) {
            info.visibleItemsInfo.also {
                // store current visible items index range
                visibleItemsRange = IntRange(it.first().index, it.last().index)

                // calculate the indicator size only once based on visible items and total items in the list
                if ((isLastItemShowing && isFirstItemShowing.not()) || indicatorSize.value == 0) {
                    indicatorSize.value = getIndicatorSize()
                }
            }
        }
    }

    private fun getIndicatorSize(): Int {
        return if (totalItemsCount != 0) {
            (((visibleItemsRange.last - visibleItemsRange.first + 1) / totalItemsCount.toFloat()) * viewportHeight).toInt()
        } else 0
    }

    private fun getItemsHeightAtTop(): Int {
        return if (totalItemsCount != 0) {
            ((visibleItemsRange.first / totalItemsCount.toFloat()) * viewportHeight).toInt()
        } else 0
    }

    private fun getItemsHeightAtBottom(): Int {
        return if (totalItemsCount != 0) {
            (((totalItemsCount - (visibleItemsRange.last + 1)) / totalItemsCount.toFloat()) * viewportHeight).toInt()
        } else 0
    }

    private fun getItemHeight(): Int {
        return if (totalItemsCount != 0) {
            ((1 / totalItemsCount.toFloat()) * viewportHeight).toInt()
        } else 0
    }
}

private fun Modifier.drawForeground(
    onDraw: DrawScope.() -> Unit
): Modifier = this.then(
    DrawForegroundModifier(
        onDraw = onDraw,
        inspectorInfo = debugInspectorInfo {
            name = "drawForeground"
            properties["onDraw"] = onDraw
        }
    )
)

private class DrawForegroundModifier(
    val onDraw: DrawScope.() -> Unit,
    inspectorInfo: InspectorInfo.() -> Unit
) : DrawModifier, InspectorValueInfo(inspectorInfo) {

    override fun ContentDrawScope.draw() {
        drawContent()
        onDraw()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DrawForegroundModifier) return false

        return onDraw == other.onDraw
    }

    override fun hashCode(): Int {
        return onDraw.hashCode()
    }
}
