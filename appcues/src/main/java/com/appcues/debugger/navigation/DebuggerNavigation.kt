package com.appcues.debugger.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.appcues.debugger.model.DebuggerCustomComponentItem
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.navigation.DebuggerRoutes.CustomComponentPage
import com.appcues.debugger.navigation.DebuggerRoutes.EventDetailsPage
import com.appcues.debugger.navigation.DebuggerRoutes.FontsPage
import com.appcues.debugger.navigation.DebuggerRoutes.LogDetailsPage
import com.appcues.debugger.navigation.DebuggerRoutes.LogsPage
import com.appcues.debugger.navigation.DebuggerRoutes.MainPage
import com.appcues.debugger.navigation.DebuggerRoutes.PluginsPage
import com.appcues.debugger.ui.SLIDE_TRANSITION_MILLIS
import com.appcues.logging.LogMessage

@Suppress("UnnecessaryAbstractClass") // False positive https://github.com/detekt/detekt/issues/2526
internal abstract class DebuggerPage(
    open val path: String,
    open val parent: DebuggerPage?,
    open val children: List<DebuggerPage>
)

internal abstract class DebuggerPageExtras<T : Any>(
    override val path: String,
    override val parent: DebuggerPage?,
    override val children: List<DebuggerPage>
) : DebuggerPage(path, parent, children) {

    private lateinit var extras: T

    fun applyExtras(extras: T) = apply {
        this.extras = extras
    }

    @Composable
    fun ComposePage(content: @Composable (T) -> Unit) {
        content(extras)
    }
}

internal object DebuggerRoutes {
    object MainPage : DebuggerPage("main", null, listOf(EventDetailsPage, FontsPage, PluginsPage, LogsPage))
    object EventDetailsPage : DebuggerPageExtras<DebuggerEventItem>("event_details", MainPage, listOf())
    object FontsPage : DebuggerPage("fonts", MainPage, listOf())
    object PluginsPage : DebuggerPage("plugins", MainPage, listOf(CustomComponentPage))
    object LogsPage : DebuggerPage("logs", MainPage, listOf(LogDetailsPage))
    object CustomComponentPage : DebuggerPageExtras<DebuggerCustomComponentItem>("custom_component", PluginsPage, listOf())
    object LogDetailsPage : DebuggerPageExtras<LogMessage>("log_details", LogsPage, listOf())
}

internal fun NavHostController.navigateDebugger(page: DebuggerPage) {
    navigate(page.path)
}

@Composable
internal fun NavHostController.ProcessDeeplinkEffect(deeplink: State<String?>, onConsumed: () -> Unit) {
    LaunchedEffect(deeplink) {
        deeplink.value.toPage()?.let {
            navigateDebugger(it)

            onConsumed()
        }
    }
}

private fun String?.toPage(): DebuggerPage? {
    return when (this) {
        MainPage.path -> MainPage
        EventDetailsPage.path -> EventDetailsPage
        FontsPage.path -> FontsPage
        PluginsPage.path -> PluginsPage
        LogsPage.path -> LogsPage
        LogDetailsPage.path -> LogDetailsPage
        CustomComponentPage.path -> CustomComponentPage
        else -> null
    }
}

internal fun <T : DebuggerPage> NavGraphBuilder.registerPage(
    page: T,
    content: @Composable T.() -> Unit
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
            Box(modifier = Modifier.testTag("route/${page.path}")) { content(page) }
        }
    )
}
