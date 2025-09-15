package com.appcues.debugger.ui

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
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.navigation.DebuggerRoutes.CustomComponentPage
import com.appcues.debugger.navigation.DebuggerRoutes.EventDetailsPage
import com.appcues.debugger.navigation.DebuggerRoutes.FontsPage
import com.appcues.debugger.navigation.DebuggerRoutes.LogDetailsPage
import com.appcues.debugger.navigation.DebuggerRoutes.LogsPage
import com.appcues.debugger.navigation.DebuggerRoutes.MainPage
import com.appcues.debugger.navigation.DebuggerRoutes.PluginsPage
import com.appcues.debugger.navigation.ProcessDeeplinkEffect
import com.appcues.debugger.navigation.registerPage
import com.appcues.debugger.ui.events.DebuggerEventDetails
import com.appcues.debugger.ui.fonts.DebuggerFontList
import com.appcues.debugger.ui.logs.DebuggerLogDetails
import com.appcues.debugger.ui.logs.DebuggerLogList
import com.appcues.debugger.ui.main.DebuggerMain
import com.appcues.debugger.ui.plugins.DebuggerCustomComponentPage
import com.appcues.debugger.ui.plugins.DebuggerPluginsPage

internal const val SLIDE_TRANSITION_MILLIS = 250

@Composable
internal fun BoxScope.DebuggerPanel(debuggerState: MutableDebuggerState, debuggerViewModel: DebuggerViewModel) {
    // don't show if current debugger is paused
    // IMPORTANT: any "remember" calls (like above) that affect the content of the expanded debugger pane, or subpages,
    // need to happen before this short-circuit return is executed, otherwise state will not be properly retained
    // on background/foreground
    if (debuggerState.isPaused.value) return

    AnimatedVisibility(
        visibleState = debuggerState.isExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(color = 0x51000000))
                .clickable { debuggerViewModel.closeExpandedView() }
        )
    }

    AnimatedVisibility(
        visibleState = debuggerState.isExpanded,
        enter = enterTransition(),
        exit = exitTransition(),
        modifier = Modifier.align(Alignment.BottomCenter)
    ) {
        Surface(
            modifier = Modifier
                .shadow(elevation = 4.dp)
                .testTag("DebuggerPanel")
                .height(debuggerState.getExpandedContainerHeight())
                .fillMaxWidth()
                .clickable(enabled = false, onClickLabel = null, onClick = { })
        ) {
            DebuggerPanelPages(debuggerViewModel)
        }
    }
}

@Composable
private fun DebuggerPanelPages(viewModel: DebuggerViewModel) {
    val navController = rememberNavController()
    val deeplink = viewModel.deeplink.collectAsState()
    val safeAreaInsets = WindowInsets.systemBars.only(WindowInsetsSides.Bottom).asPaddingValues()
    navController.ProcessDeeplinkEffect(deeplink) { viewModel.consumeDeeplink() }

    NavHost(
        navController = navController,
        startDestination = MainPage.path,
        Modifier.padding(safeAreaInsets)
    ) {
        registerPage(MainPage) {
            DebuggerMain(viewModel, navController)
        }

        registerPage(EventDetailsPage) {
            ComposePage { DebuggerEventDetails(it, navController) }
        }

        registerPage(FontsPage) {
            DebuggerFontList(viewModel, navController)
        }

        registerPage(PluginsPage) {
            DebuggerPluginsPage(viewModel, navController)
        }

        registerPage(LogsPage) {
            DebuggerLogList(viewModel, navController)
        }

        registerPage(LogDetailsPage) {
            ComposePage { DebuggerLogDetails(it, navController) }
        }

        registerPage(CustomComponentPage) {
            ComposePage { DebuggerCustomComponentPage(it, navController) }
        }
    }
}

private fun enterTransition(): EnterTransition {
    return slideInVertically(tween(durationMillis = SLIDE_TRANSITION_MILLIS)) { it }
}

private fun exitTransition(): ExitTransition {
    return slideOutVertically(tween(durationMillis = SLIDE_TRANSITION_MILLIS)) { it } +
        fadeOut(tween(durationMillis = 150))
}
