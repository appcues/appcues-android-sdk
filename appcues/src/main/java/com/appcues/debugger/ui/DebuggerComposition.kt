package com.appcues.debugger.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.LifecycleEventObserver
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.DebuggerViewModel.UIState.Creating
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissed
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.debugger.DebuggerViewModel.UIState.Dragging
import com.appcues.debugger.DebuggerViewModel.UIState.Expanded
import com.appcues.debugger.DebuggerViewModel.UIState.Idle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
internal fun DebuggerComposition(viewModel: DebuggerViewModel, onDismiss: () -> Unit) {
    val density = LocalDensity.current
    val debuggerState by remember { mutableStateOf(MutableDebuggerState(density, viewModel.uiState.value == Creating)) }

    // listening for lifecycle changes to update isPaused properly
    LocalLifecycleOwner.current.lifecycle.observeAsSate().let {
        debuggerState.isPaused.value = it.value == Event.ON_PAUSE
    }

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
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
        )

        // Fab is last because it will show on top of everything else in this composition
        DebuggerFloatingActionButton(
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
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
    viewModel.uiState.collectAsState().value.let { state ->
        LaunchedEffect(state) {
            when (state) {
                Creating -> Unit
                Idle -> {
                    debuggerState.isVisible.targetState = true
                    // lets only animate to idle when we come form isExpanded
                    if (debuggerState.isExpanded.targetState) {
                        animateFabToIdle(debuggerState)

                        debuggerState.isExpanded.targetState = false
                    }
                    // lets anchor to the side when we come from isDragging
                    if (debuggerState.isDragging.targetState) {
                        animateToAnchor(debuggerState)

                        debuggerState.isDragging.targetState = false
                    }
                }
                is Dragging -> {
                    debuggerState.isExpanded.targetState = false
                    debuggerState.isDragging.targetState = true
                    debuggerState.dragFabOffsets(state.dragAmount)
                }
                Expanded -> {
                    animateFabToExpanded(debuggerState)

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

private fun CoroutineScope.animateToAnchor(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.value).animateTo(
                targetValue = getLastAnchoredPosition().x,
                animationSpec = tween(durationMillis = 300)
            ) {
                fabXOffset.value = value
            }
        }
    }
}

private fun CoroutineScope.animateFabToIdle(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.value).animateTo(
                targetValue = getLastAnchoredPosition().x,
                animationSpec = tween(durationMillis = 350)
            ) {
                fabXOffset.value = value
            }
        }

        launch {
            Animatable(fabYOffset.value).animateTo(
                targetValue = getLastAnchoredPosition().y,
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

@Composable
private fun Lifecycle.observeAsSate(): MutableState<Event> {
    val state = remember { mutableStateOf(ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsSate.addObserver(observer)
        onDispose {
            this@observeAsSate.removeObserver(observer)
        }
    }
    return state
}
