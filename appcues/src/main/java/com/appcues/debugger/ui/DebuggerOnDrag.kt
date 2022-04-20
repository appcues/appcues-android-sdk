package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.R.drawable
import com.appcues.ui.theme.AppcuesColors

@Composable
internal fun BoxScope.DebuggerOnDrag(
    debuggerState: MutableDebuggerState,
    onDismiss: () -> Unit,
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
        DismissDebuggerArea { debuggerState.initDismissAreaRect(it) }
    }

    // this means that debugger fab is not being dragged anymore and
    // its area is overlapping with dismissing area
    with(debuggerState.isDragging) {
        LaunchedEffect(targetState) {
            if (targetState.not() && debuggerState.isFabInDismissingArea()) {
                onDismiss()
            }
        }
    }
}

@Composable
private fun DismissDebuggerArea(onGloballyPositioned: (LayoutCoordinates) -> Unit) {
    Box(
        modifier = Modifier
            .background(
                brush = Brush.radialGradient(
                    listOf(AppcuesColors.BlackAlmostTransparent, Color.Transparent)
                )
            )
            .size(168.dp)
            .onGloballyPositioned { onGloballyPositioned(it) },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawable.ic_dismiss),
            modifier = Modifier.clip(RoundedCornerShape(percent = 100)),
            contentDescription = LocalContext.current.getString(R.string.debugger_fab_dismiss_image_content_description)
        )
    }
}
