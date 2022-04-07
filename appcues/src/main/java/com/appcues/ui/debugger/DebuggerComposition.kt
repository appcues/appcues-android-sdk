package com.appcues.ui.debugger

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
internal fun DebuggerComposition(onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val isVisible = rememberVisibilityState(onDismiss)

    AnimatedVisibility(
        visibleState = isVisible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val boxSize = remember { mutableStateOf(IntSize(0, 0)) }
        val fabSize = 56.dp
        val offsetX = remember { mutableStateOf(value = 0f) }
        val offsetY = remember { mutableStateOf(value = 0f) }

        LaunchInitialOffsetPositions(boxSize, fabSize, offsetX, offsetY)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { boxSize.value = it.size }
        ) {
            val isDragging = remember { MutableTransitionState(false) }
            val dismissRect = remember { mutableStateOf(Rect(Offset(0f, 0f), Size(0f, 0f))) }
            val debuggerFabRect = Rect(
                offset = Offset(offsetX.value, offsetY.value),
                size = with(density) { Size(fabSize.toPx(), fabSize.toPx()) }
            )

            DismissDebugger(
                isDragging = isDragging,
                dismissRect = dismissRect,
            )

            val context = LocalContext.current
            DebuggerFloatingActionButton(
                isVisible = isVisible,
                isDragging = isDragging,
                parentSize = boxSize,
                offsetX = offsetX,
                offsetY = offsetY,
                size = fabSize,
            ) {
                Toast.makeText(context, "teste click", Toast.LENGTH_SHORT).show()
            }

            LaunchPostDraggingEffects(isDragging, dismissRect, debuggerFabRect, offsetX, offsetY, isVisible)
        }
    }
}

private const val GRID_SCREEN_COUNT = 5
private const val GRID_FAB_POSITION = 4

@Composable
private fun LaunchInitialOffsetPositions(
    boxSize: MutableState<IntSize>,
    fabSize: Dp,
    offsetX: MutableState<Float>,
    offsetY: MutableState<Float>
) {
    val density = LocalDensity.current
    LaunchedEffect(boxSize) {
        with(density) {
            offsetX.value = boxSize.value.width.toFloat() - fabSize.toPx()
            offsetY.value = ((boxSize.value.height.toFloat() / GRID_SCREEN_COUNT) * GRID_FAB_POSITION) - fabSize.toPx()
        }
    }
}

@Composable
private fun LaunchPostDraggingEffects(
    isDragging: MutableTransitionState<Boolean>,
    dismissRect: MutableState<Rect>,
    debuggerFabRect: Rect,
    offsetX: MutableState<Float>,
    offsetY: MutableState<Float>,
    isVisible: MutableTransitionState<Boolean>
) {
    LaunchedEffect(isDragging.targetState) {
        if (isDragging.targetState.not()) {

            if (dismissRect.value.overlaps(debuggerFabRect)) {
                val channel = Channel<Boolean>()
                launch {
                    Animatable(debuggerFabRect.topLeft.x).animateTo(
                        targetValue = dismissRect.value.center.x - debuggerFabRect.size.width / 2,
                        animationSpec = tween(durationMillis = 300)
                    ) {
                        offsetX.value = value
                    }

                    channel.send(true)
                }

                launch {
                    Animatable(debuggerFabRect.topLeft.y).animateTo(
                        targetValue = dismissRect.value.center.y - debuggerFabRect.size.height / 2,
                        animationSpec = tween(durationMillis = 300)
                    ) {
                        offsetY.value = value
                    }

                    channel.send(true)
                }

                launch {
                    var waitComplete = 2
                    while (waitComplete > 0) {
                        channel.receive()
                        waitComplete -= 1
                    }

                    isVisible.targetState = false
                }
            }
        }
    }
}

@Composable
private fun rememberVisibilityState(onDismiss: () -> Unit): MutableTransitionState<Boolean> {
    return remember { MutableTransitionState(false) }.also {
        // On first composition, set target to visible
        LaunchedEffect(Unit) {
            it.targetState = true
        }

        LaunchedEffect(it.currentState) {
            // when current state and target state are set to hide and animation is idle
            // we will call back to onDismiss
            if (it.isIdle && it.currentState.not() && it.targetState.not()) {
                onDismiss()
            }
        }
    }
}
