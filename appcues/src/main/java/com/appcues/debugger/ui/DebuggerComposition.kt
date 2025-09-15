package com.appcues.debugger.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_ANY
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebugMode.ScreenCapture
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.DebuggerViewModel.ToastState
import com.appcues.debugger.DebuggerViewModel.ToastState.Rendering
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
    val uiState = viewModel.uiState.collectAsState()
    val density = LocalDensity.current
    val safeAreaInsets = WindowInsets.systemBars.asPaddingValues()
    val debuggerState by remember(uiState.value.mode, safeAreaInsets) {
        mutableStateOf(MutableDebuggerState(uiState.value.mode, density, safeAreaInsets, uiState.value is Creating))
    }

    // listening for lifecycle changes to update isPaused properly
    LocalLifecycleOwner.current.lifecycle.observeAsState().let {
        debuggerState.isPaused.value = it.value == Event.ON_PAUSE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { debuggerState.initFabOffsets(it) }
            .testTag("debugger-root")
    ) {

        DebuggerOnDrag(
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
        )

        DebuggerPanel(
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
        )

        // Fab is on top of debugger panel in this composition
        DebuggerFloatingActionButton(
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
        )

        DebuggerFloatingActionEvents(
            debuggerState = debuggerState,
            debuggerViewModel = viewModel,
        )

        val capture = debuggerState.screenCapture.value
        if (capture != null) {
            CaptureConfirmDialog(
                capture = capture,
                debuggerState = debuggerState,
                debuggerViewModel = viewModel
            )
        }

        ToastView(debuggerState = debuggerState)
    }

    with(debuggerState.isVisible) {
        LaunchedEffect(currentState) {
            if (isIdle && currentState.not() && targetState.not()) {
                // when current state and target state are set to hide and animation is idle
                // we will call back to onDismiss
                viewModel.onDismissAnimationCompleted(debuggerState)
            }
        }
    }

    LaunchedUIStateEffect(
        viewModel = viewModel,
        debuggerState = debuggerState,
        onDismiss = onDismiss,
    )

    LaunchedToastStateEffect(
        viewModel = viewModel,
        debuggerState = debuggerState,
    )

    // run once to transition state in viewModel
    LaunchedEffect(Unit) {
        viewModel.onRender()
    }
}

@Composable
private fun LaunchedToastStateEffect(
    viewModel: DebuggerViewModel,
    debuggerState: MutableDebuggerState,
) {
    viewModel.toastState.collectAsState().value.let {
        when (it) {
            is ToastState.Idle -> {
                debuggerState.toast.targetState = null
            }
            is Rendering -> {
                debuggerState.toast.targetState = it.type
            }
        }
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
                is Creating -> {
                    debuggerState.isVisible.targetState = true
                }
                is Idle -> {
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
                    // clear any pending screen capture
                    debuggerState.screenCapture.value = null
                }
                is Dragging -> {
                    debuggerState.isExpanded.targetState = false
                    debuggerState.isDragging.targetState = true
                    debuggerState.dragFabOffsets(state.dragAmount)
                }
                is Expanded -> {
                    when (state.mode) {
                        is Debugger -> {
                            animateFabToExpanded(debuggerState)
                            debuggerState.isExpanded.targetState = true
                        }
                        is ScreenCapture -> {
                            // hide FAB
                            debuggerState.isVisible.targetState = false
                        }
                    }
                }
                is Dismissing -> {
                    animateFabToDismiss(
                        debuggerState = debuggerState,
                        onComplete = { viewModel.onDismissAnimationCompleted(debuggerState) },
                    )

                    debuggerState.isVisible.targetState = false
                }
                is Dismissed -> onDismiss()
            }
        }
    }
}

private fun CoroutineScope.animateFabToExpanded(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.floatValue).animateTo(
                targetValue = getExpandedFabAnchor().x,
                animationSpec = tween(durationMillis = 250)
            ) {
                fabXOffset.floatValue = value
            }
        }

        launch {
            Animatable(fabYOffset.floatValue).animateTo(
                targetValue = getExpandedFabAnchor().y,
                animationSpec = tween(durationMillis = 350)
            ) {
                fabYOffset.floatValue = value
            }
        }
    }
}

private fun CoroutineScope.animateToAnchor(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.floatValue).animateTo(
                targetValue = getLastAnchoredPosition().x,
                animationSpec = tween(durationMillis = 300)
            ) {
                fabXOffset.floatValue = value
            }
        }
    }
}

private fun CoroutineScope.animateFabToIdle(debuggerState: MutableDebuggerState) {
    with(debuggerState) {
        launch {
            Animatable(fabXOffset.floatValue).animateTo(
                targetValue = getLastAnchoredPosition().x,
                animationSpec = tween(durationMillis = 350)
            ) {
                fabXOffset.floatValue = value
            }
        }

        launch {
            Animatable(fabYOffset.floatValue).animateTo(
                targetValue = getLastAnchoredPosition().y,
                animationSpec = tween(durationMillis = 250)
            ) {
                fabYOffset.floatValue = value
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
            Animatable(fabXOffset.floatValue).animateTo(
                targetValue = getDismissAreaTargetXOffset(),
                animationSpec = tween(durationMillis = 300)
            ) {
                fabXOffset.floatValue = value
            }

            channel.send(true)
        }

        launch {
            Animatable(fabYOffset.floatValue).animateTo(
                targetValue = getDismissAreaTargetYOffset(),
                animationSpec = tween(durationMillis = 300)
            ) {
                fabYOffset.floatValue = value
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
private fun Lifecycle.observeAsState(): MutableState<Event> {
    val state = remember { mutableStateOf(ON_ANY) }
    DisposableEffect(this) {
        val observer = LifecycleEventObserver { _, event ->
            state.value = event
        }
        this@observeAsState.addObserver(observer)
        onDispose {
            this@observeAsState.removeObserver(observer)
        }
    }
    return state
}
