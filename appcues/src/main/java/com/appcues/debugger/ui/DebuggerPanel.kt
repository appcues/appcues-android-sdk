package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
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
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.DebuggerPages.EventDetailsPage
import com.appcues.debugger.ui.DebuggerPages.ExpandedLogDetailsPage
import com.appcues.debugger.ui.DebuggerPages.FontListPage
import com.appcues.debugger.ui.DebuggerPages.LogListPage
import com.appcues.debugger.ui.DebuggerPages.MainPage
import com.appcues.debugger.ui.events.DebuggerEventDetails
import com.appcues.debugger.ui.fonts.DebuggerFontList
import com.appcues.debugger.ui.logs.DebuggerLogDetails
import com.appcues.debugger.ui.logs.DebuggerLogList
import com.appcues.debugger.ui.main.DebuggerMain
import com.appcues.logging.LogMessage

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

internal sealed class DebuggerPages(val path: String, val parent: DebuggerPages?, val children: List<DebuggerPages>) {
    object MainPage : DebuggerPages("main", null, listOf(EventDetailsPage, FontListPage, LogListPage))
    object EventDetailsPage : DebuggerPages("event_details", MainPage, listOf())
    object FontListPage : DebuggerPages("fonts", MainPage, listOf())
    object LogListPage : DebuggerPages("logs", MainPage, listOf(ExpandedLogDetailsPage))
    object ExpandedLogDetailsPage : DebuggerPages("log_details", LogListPage, listOf())
}

internal fun NavHostController.navigateDebugger(page: DebuggerPages) {
    navigate(page.path)
}

private fun String?.toPage(): DebuggerPages? {
    return when (this) {
        MainPage.path -> MainPage
        EventDetailsPage.path -> EventDetailsPage
        FontListPage.path -> FontListPage
        LogListPage.path -> LogListPage
        ExpandedLogDetailsPage.path -> ExpandedLogDetailsPage
        else -> null
    }
}

@Composable
private fun DebuggerPanelPages(viewModel: DebuggerViewModel) {
    val navController = rememberNavController()
    val selectedEvent = remember { mutableStateOf<DebuggerEventItem?>(null) }
    val selectedLogMessage = remember { mutableStateOf<LogMessage?>(null) }

    NavHost(navController = navController, startDestination = MainPage.path) {
        registerPage(MainPage) {
            DebuggerMain(
                debuggerViewModel = viewModel,
                onEventClick = {
                    selectedEvent.value = it
                    navController.navigateDebugger(EventDetailsPage)
                },
                onFontsClick = { navController.navigateDebugger(FontListPage) },
                onDetailedLogClick = { navController.navigateDebugger(LogListPage) }
            )
        }

        registerPage(EventDetailsPage) {
            selectedEvent.value?.let {
                DebuggerEventDetails(it, navController)
            }
        }

        registerPage(FontListPage) {
            DebuggerFontList(viewModel, navController)
        }

        registerPage(LogListPage) {
            DebuggerLogList(viewModel, navController) {
                selectedLogMessage.value = it
                navController.navigateDebugger(ExpandedLogDetailsPage)
            }
        }

        registerPage(ExpandedLogDetailsPage) {
            selectedLogMessage.value?.let {
                DebuggerLogDetails(it, navController)
            }
        }
    }

    val deeplink = viewModel.deeplink.collectAsState()
    LaunchedEffect(deeplink.value) {
        deeplink.value.toPage()?.let {
            navController.navigateDebugger(it)
            viewModel.consumeDeeplink()
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

private fun NavGraphBuilder.registerPage(
    page: DebuggerPages,
    content: @Composable () -> Unit
) {
    composable(
        route = page.path,
        enterTransition = {
            val destination = initialState.destination.route.toPage()

            when {
                page.parent == destination -> slideIntoContainer(SlideDirection.Start, tween(SLIDE_TRANSITION_MILLIS))
                page.children.contains(destination) -> slideIntoContainer(SlideDirection.End, tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        exitTransition = {
            val destination = targetState.destination.route.toPage()

            when {
                page.parent == destination -> slideOutOfContainer(SlideDirection.End, tween(SLIDE_TRANSITION_MILLIS))
                page.children.contains(destination) -> slideOutOfContainer(SlideDirection.Start, tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        content = {
            Box(modifier = Modifier.testTag("route/${page.path}")) { content() }
        }
    )
}
