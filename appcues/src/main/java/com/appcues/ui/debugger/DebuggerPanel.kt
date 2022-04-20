package com.appcues.ui.debugger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
internal fun BoxScope.DebuggerPanel(debuggerState: MutableDebuggerState, debuggerViewModel: DebuggerViewModel) {
    // don't show if current debugger is paused
    if (debuggerState.isPaused.value) return

    AnimatedVisibility(
        visibleState = debuggerState.isExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color = 0x30000000))
                .clickable { debuggerViewModel.onBackdropClick() }
        )
    }

    AnimatedVisibility(
        visibleState = debuggerState.isExpanded,
        enter = enterTransition(),
        exit = exitTransition(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation = 4.dp)
                .height(debuggerState.getExpandedContainerHeight())
                .fillMaxWidth()
                .background(Color(color = 0xFFFFFFFF))
                .clickable(enabled = false, onClickLabel = null) {}
                // adding padding top to make sure nothing is drawn below the FAB
                .padding(top = 40.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            NavHost(navController = rememberNavController(), startDestination = "main") {
                composable("main") { DebuggerMain(debuggerViewModel) }
                composable("event_details") { DebuggerEventDetails() }
            }
        }
    }
}

private fun enterTransition(): EnterTransition {
    return slideInVertically(tween(durationMillis = 250)) { it }
}

private fun exitTransition(): ExitTransition {
    return slideOutVertically(tween(durationMillis = 200)) { it } +
        fadeOut(tween(durationMillis = 150))
}

@Composable
internal fun DebuggerEventDetails() {
    Text(text = "Event Details")
}
