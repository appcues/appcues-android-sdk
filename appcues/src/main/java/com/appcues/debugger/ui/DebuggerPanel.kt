package com.appcues.debugger.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.details.DebuggerEventDetails
import com.appcues.debugger.ui.fonts.DebuggerFontDetails
import com.appcues.debugger.ui.main.DebuggerMain
import com.appcues.ui.theme.AppcuesColors
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

private const val SLIDE_TRANSITION_MILLIS = 250

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BoxScope.DebuggerPanel(debuggerState: MutableDebuggerState, debuggerViewModel: DebuggerViewModel) {
    val navController = rememberAnimatedNavController()
    val selectedEvent = remember { mutableStateOf<DebuggerEventItem?>(null) }

    // don't show if current debugger is paused
    // IMPORTANT: any "remember" calls (like above) that affect the content of the expanded debugger pane, or subpages,
    // need to happen before this short-circuit return is executed, otherwise state will not be properly retained
    // on background/foreground
    if (debuggerState.isPaused.value) return

    // deeplink (if applicable) is used once, then reset
    val deeplinkPath = debuggerState.deeplinkPath.value
    debuggerState.deeplinkPath.value = null

    AnimatedVisibility(
        visibleState = debuggerState.isExpanded,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(AppcuesColors.DebuggerBackdrop)
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
                .background(Color.White)
                .clickable(enabled = false, onClickLabel = null) {},
            contentAlignment = Alignment.TopCenter
        ) {
            DebuggerPanelPages(navController, selectedEvent, debuggerViewModel, deeplinkPath)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxScope.DebuggerPanelPages(
    navController: NavHostController,
    selectedEvent: MutableState<DebuggerEventItem?>,
    debuggerViewModel: DebuggerViewModel,
    deeplinkPath: String?
) {
    val mainPage = "main"
    val eventDetailsPage = "event_details"
    val fontDetailsPage = "font_details"

    AnimatedNavHost(navController = navController, startDestination = mainPage) {
        mainComposable(
            pageName = mainPage,
            eventDetailsPage = eventDetailsPage
        ) {
            DebuggerMain(
                debuggerViewModel = debuggerViewModel,
                onEventClick = {
                    selectedEvent.value = it
                    navController.navigate(eventDetailsPage)
                },
                onFontsClick = {
                    navController.navigate(fontDetailsPage)
                },
            )

            LaunchedEffect(Unit) {
                when (deeplinkPath) {
                    "fonts" -> navController.navigate(fontDetailsPage)
                    else -> Unit
                }
            }
        }

        eventDetailsComposable(eventDetailsPage, mainPage, selectedEvent, navController)

        fontDetailsComposable(fontDetailsPage, mainPage, debuggerViewModel, navController)
    }
}

private fun enterTransition(): EnterTransition {
    return slideInVertically(tween(durationMillis = SLIDE_TRANSITION_MILLIS)) { it }
}

private fun exitTransition(): ExitTransition {
    return slideOutVertically(tween(durationMillis = SLIDE_TRANSITION_MILLIS)) { it } +
        fadeOut(tween(durationMillis = 150))
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.mainComposable(
    pageName: String,
    eventDetailsPage: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = pageName,
        enterTransition = {
            when (initialState.destination.route) {
                eventDetailsPage ->
                    slideIntoContainer(AnimatedContentScope.SlideDirection.End, animationSpec = tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                eventDetailsPage ->
                    slideOutOfContainer(AnimatedContentScope.SlideDirection.Start, animationSpec = tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.detailPageComposable(
    pageName: String,
    mainPage: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = pageName,
        enterTransition = {
            when (initialState.destination.route) {
                mainPage ->
                    slideIntoContainer(AnimatedContentScope.SlideDirection.Start, animationSpec = tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        exitTransition = {
            when (targetState.destination.route) {
                mainPage ->
                    slideOutOfContainer(AnimatedContentScope.SlideDirection.End, animationSpec = tween(SLIDE_TRANSITION_MILLIS))
                else -> null
            }
        },
        content = content
    )
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.eventDetailsComposable(
    pageName: String,
    mainPage: String,
    selectedEvent: MutableState<DebuggerEventItem?>,
    navController: NavController,
) {
    detailPageComposable(
        pageName = pageName,
        mainPage = mainPage
    ) {
        DebuggerEventDetails(selectedEvent.value) {
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun NavGraphBuilder.fontDetailsComposable(
    pageName: String,
    mainPage: String,
    debuggerViewModel: DebuggerViewModel,
    navController: NavController,
) {
    detailPageComposable(
        pageName = pageName,
        mainPage = mainPage
    ) {
        val clipboard = LocalClipboardManager.current
        val context = LocalContext.current

        DebuggerFontDetails(
            appSpecificFonts = debuggerViewModel.appSpecificFonts,
            systemFonts = debuggerViewModel.systemFonts,
            allFonts = debuggerViewModel.allFonts,
            onFontTap = {
                clipboard.setText(AnnotatedString(it.name))
                Toast.makeText(
                    context,
                    context.getString(R.string.appcues_debugger_font_details_clipboard_message),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onBackPressed = {
                navController.popBackStack()
            }
        )
    }
}
