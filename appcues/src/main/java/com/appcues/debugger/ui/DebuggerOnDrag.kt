package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.R.drawable
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.DebuggerViewModel.UIState.Dismissing
import com.appcues.ui.theme.AppcuesColors

@Composable
internal fun BoxScope.DebuggerOnDrag(
    debuggerState: MutableDebuggerState,
    debuggerViewModel: DebuggerViewModel,
) {
    // don't show if current debugger is paused
    if (debuggerState.isPaused.value) return

    // show content when we are dragging
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visibleState = debuggerState.isDragging,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        DismissDebuggerArea(debuggerState) { debuggerState.initDismissAreaRect(it) }
    }

    // this means that debugger fab is not being dragged anymore and
    // its area is overlapping with dismissing area
    with(debuggerState.isDragging) {
        LaunchedEffect(targetState) {
            if (targetState.not() && debuggerState.isDraggingOverDismiss.value) {
                debuggerViewModel.transition(Dismissing(debuggerState.debugMode))
            }
        }
    }
}

private const val ROTATE_90_DEGREES = 90f
private const val ROTATE_NONE = 0f

@Composable
private fun DismissDebuggerArea(debuggerState: MutableDebuggerState, onGloballyPositioned: (LayoutCoordinates) -> Unit) {
    val isDraggingAndColliding = debuggerState.isDragging.targetState && debuggerState.isDraggingOverDismiss.value
    val size = animateDpAsState(
        if (isDraggingAndColliding) 50.dp else 44.dp,
        label = "Fab dismissing Size"
    )
    val rotate = animateFloatAsState(
        if (isDraggingAndColliding) ROTATE_90_DEGREES else ROTATE_NONE,
        label = "Fab dismissing Rotation"
    )

    Box(
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    listOf(AppcuesColors.DebuggerDismissArea, Color.Transparent)
                )
            )
            .size(168.dp)
            .onGloballyPositioned { onGloballyPositioned(it) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawable.appcues_ic_debugger_dismiss),
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 100))
                .size(size.value)
                .rotate(rotate.value),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_fab_dismiss_image_content_description)
        )
    }
}
