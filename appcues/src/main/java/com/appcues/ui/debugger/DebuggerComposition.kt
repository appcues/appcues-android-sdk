package com.appcues.ui.debugger

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Dragging
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.ui.debugger.DebuggerViewModel.UIState.Idle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
internal fun DebuggerComposition(viewModel: DebuggerViewModel, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val debuggerState by remember { mutableStateOf(MutableDebuggerState(density)) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { debuggerState.initFabOffsets(it) }
    ) {

        DebuggerOnDrag(
            debuggerState = debuggerState,
            onDismiss = { viewModel.onDismiss() }
        )

        DebuggerPanel(
            debuggerState = debuggerState
        )

        // Fab is last because it will show on top of everything else in this composition
        DebuggerFloatingActionButton(
            debuggerState = debuggerState,
            onDragStart = { viewModel.onDragStart() },
            onDragEnd = { viewModel.onDragEnd() },
            onDrag = { debuggerState.updateFabOffsets(it) },
            onClick = { viewModel.onFabClick() }
        )
    }

    with(debuggerState.isVisible) {
        LaunchedEffect(currentState) {
            if (isIdle && currentState.not() && targetState.not()) {
                // when current state and target state are set to hide and animation is idle
                // we will call back to onDismiss
                viewModel.onDismissAnimationCompleted()
            }
        }
    }

    LaunchedUIStateEffect(
        viewModel = viewModel,
        debuggerState = debuggerState,
        onDismiss = onDismiss,
    )

    // run once to transition state in viewModel
    LaunchedEffect(Unit) {
        viewModel.onInit()
    }
}

@Composable
private fun LaunchedUIStateEffect(
    viewModel: DebuggerViewModel,
    debuggerState: MutableDebuggerState,
    onDismiss: () -> Unit
) {
    with(viewModel.uiState.collectAsState()) {
        LaunchedEffect(value) {
            when (value) {
                Creating -> Unit
                Idle -> {
                    animateFabToIdle(debuggerState = debuggerState)

                    debuggerState.isVisible.targetState = true
                    debuggerState.isDragging.targetState = false
                    debuggerState.isExpanded.targetState = false
                }
                Dragging -> {
                    debuggerState.isDragging.targetState = true
                }
                // next work will be focused on the expanded state
                Expanded -> {
                    animateFabToExpanded(debuggerState = debuggerState)

                    debuggerState.isExpanded.targetState = true
                }
                Dismissing -> {
                    animateFabToDismiss(
                        debuggerState = debuggerState,
                        onComplete = { viewModel.onDismissAnimationCompleted() },
                    )

                    debuggerState.isVisible.targetState = false
                }
                Dismissed -> onDismiss()
            }
        }
    }
}

private fun CoroutineScope.animateFabToExpanded(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.value).animateTo(
                targetValue = getExpandedFabAnchor().x,
                animationSpec = tween(durationMillis = 250)
            ) {
                fabXOffset.value = value
            }
        }

        launch {
            Animatable(fabYOffset.value).animateTo(
                targetValue = getExpandedFabAnchor().y,
                animationSpec = tween(durationMillis = 350)
            ) {
                fabYOffset.value = value
            }
        }
    }
}

private fun CoroutineScope.animateFabToIdle(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        if (shouldAnimateToIdle().not()) return

        launch {
            Animatable(fabXOffset.value).animateTo(
                targetValue = getLastIdleFabAnchor().x,
                animationSpec = tween(durationMillis = 350)
            ) {
                fabXOffset.value = value
            }
        }

        launch {
            Animatable(fabYOffset.value).animateTo(
                targetValue = getLastIdleFabAnchor().y,
                animationSpec = tween(durationMillis = 250)
            ) {
                fabYOffset.value = value
            }
        }
    }
}

private fun CoroutineScope.animateFabToDismiss(
    debuggerState: MutableDebuggerState,
    onComplete: () -> Unit,
) {
    with(debuggerState) {
        val channel = Channel<Boolean>()
        launch {
            Animatable(fabXOffset.value).animateTo(
                targetValue = getDismissAreaTargetXOffset(),
                animationSpec = tween(durationMillis = 300)
            ) {
                fabXOffset.value = value
            }

            channel.send(true)
        }

        launch {
            Animatable(fabYOffset.value).animateTo(
                targetValue = getDismissAreaTargetYOffset(),
                animationSpec = tween(durationMillis = 300)
            ) {
                fabYOffset.value = value
            }

            channel.send(true)
        }

        launch {
            var waitComplete = 2
            while (waitComplete > 0) {
                channel.receive()
                waitComplete -= 1
            }

            onComplete()
        }
    }
}
