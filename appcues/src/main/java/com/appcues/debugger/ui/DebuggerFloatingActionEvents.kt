package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.debugger.DebugMode.Debugger
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import kotlinx.coroutines.delay

private const val MAX_FAB_EVENTS = 10
private const val FAB_EVENT_DISPLAYED_ALPHA = 0.50f
private const val EVENT_VISIBILITY_DELAY = 1000L

@Composable
internal fun BoxScope.DebuggerFloatingActionEvents(
    debuggerState: MutableDebuggerState,
    debuggerViewModel: DebuggerViewModel,
) {
    val eventsProperties = debuggerState.getEventsProperties()

    // this organize elements in a row from left to right or right to left
    CompositionLocalProvider(
        LocalLayoutDirection provides if (eventsProperties.anchorToStart) LayoutDirection.Ltr else LayoutDirection.Rtl
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .align(
                    when {
                        eventsProperties.anchorToStart && eventsProperties.drawTop -> Alignment.BottomStart
                        eventsProperties.anchorToStart -> Alignment.TopStart
                        eventsProperties.drawTop -> Alignment.BottomEnd
                        else -> Alignment.TopEnd
                    }
                )
                .offset { eventsProperties.positionOffset },
            contentAlignment = Alignment.TopEnd
        ) {
            val shouldDisplayEvents = debuggerState.debugMode is Debugger &&
                debuggerState.isDragging.targetState.not() &&
                debuggerState.isExpanded.targetState.not()

            val events = if (shouldDisplayEvents)
                debuggerViewModel.events.collectAsState().value.take(MAX_FAB_EVENTS)
            else
                arrayListOf<DebuggerEventItem>().also { debuggerViewModel.hideAllEvents() }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                reverseLayout = eventsProperties.drawTop,
                userScrollEnabled = false,
            ) {
                items(events, key = { it.id }) { event ->
                    Item(event, eventsProperties.anchorToStart) { debuggerViewModel.onDisplayedEventTimeout(it) }
                }
            }
        }
    }
}

@Composable
private fun LazyItemScope.Item(event: DebuggerEventItem, isAnchoredStart: Boolean, eventTimedOut: (Int) -> Unit) {
    val theme = LocalAppcuesTheme.current
    val visibility = remember { MutableTransitionState(false) }.apply { targetState = event.showOnFab }
    val displayed = remember { mutableStateOf(false) }
    val alpha = animateFloatAsState(
        targetValue = if (displayed.value) FAB_EVENT_DISPLAYED_ALPHA else 1f,
        animationSpec = tween(
            durationMillis = EVENT_VISIBILITY_DELAY.toInt(),
            easing = FastOutLinearInEasing
        ),
        label = "Floating actions Alpha"
    )

    AnimatedVisibility(
        enter = slideInHorizontally(initialOffsetX = { if (isAnchoredStart) -it else it }),
        exit = fadeOut(),
        visibleState = visibility
    ) {
        Row(
            modifier = Modifier
                .padding(2.dp)
                .shadow(2.dp, RoundedCornerShape(6.dp), clip = true)
                .clip(RoundedCornerShape(6.dp))
                .background(theme.background)
                .alpha(alpha.value)
                .padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 2.dp)
                .animateItem(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Image(
                modifier = Modifier.size(14.dp),
                colorFilter = ColorFilter.tint(theme.secondary),
                painter = painterResource(id = event.type.toResourceId()),
                contentDescription = LocalContext.current.getString(event.type.getTitleString())
            )

            Text(fontSize = 12.sp, text = event.name, color = theme.secondary)
        }
    }

    LaunchedEffect(event.id) {
        delay(EVENT_VISIBILITY_DELAY)
        displayed.value = true
        delay(EVENT_VISIBILITY_DELAY)
        visibility.targetState = false
        eventTimedOut(event.id)
    }
}
