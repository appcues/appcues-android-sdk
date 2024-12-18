package com.appcues.debugger.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.model.DebuggerInfoItem
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.EventType
import com.appcues.debugger.model.StatusType
import com.appcues.debugger.model.TapActionType
import com.appcues.debugger.navigation.DebuggerRoutes.EventDetailsPage
import com.appcues.debugger.navigation.DebuggerRoutes.FontsPage
import com.appcues.debugger.navigation.DebuggerRoutes.LogsPage
import com.appcues.debugger.navigation.DebuggerRoutes.PluginsPage
import com.appcues.debugger.navigation.navigateDebugger
import com.appcues.debugger.ui.ds.DividerItem
import com.appcues.debugger.ui.ds.TextHeader
import com.appcues.debugger.ui.getTitleString
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.debugger.ui.toResourceId
import kotlinx.coroutines.delay

@Composable
internal fun DebuggerMain(
    debuggerViewModel: DebuggerViewModel,
    navController: NavHostController,
) {
    val statusInfo = debuggerViewModel.statusInfo.collectAsState()
    val recentEvents = debuggerViewModel.events.collectAsState()
    val isFilterOn = debuggerViewModel.currentFilter.collectAsState()

    val lazyListState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .lazyColumnScrollIndicator(lazyListState),
        state = lazyListState
    ) {
        item { Spacer(modifier = Modifier.height(40.dp)) }

        statusSection(statusInfo.value) { debuggerViewModel.onStatusTapAction(it) }

        infoSection(navController)

        eventsSection(navController, isFilterOn.value, recentEvents.value) { debuggerViewModel.onApplyEventFilter(it) }
    }
}

private fun LazyListScope.statusSection(list: List<DebuggerStatusItem>, onTap: (TapActionType) -> Unit) {
    item {
        TextHeader(
            modifier = Modifier.padding(start = 40.dp),
            text = LocalContext.current.getString(R.string.appcues_debugger_status_title)
        )
    }

    items(list) { item ->
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .then(
                    if (item.tapActionType != null && item.statusType != StatusType.LOADING)
                        Modifier.clickable { onTap(item.tapActionType) } else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(item) {
                StatusItemIcon()

                StatusItemContent(rowScope = this@Row)

                RefreshIcon()
            }
        }

        DividerItem()
    }
}

private fun LazyListScope.infoSection(navController: NavHostController) {
    item {
        Box(
            modifier = Modifier
                .fillParentMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            TextHeader(
                modifier = Modifier.padding(start = 40.dp),
                text = LocalContext.current.getString(R.string.appcues_debugger_info_title)
            )
        }
    }

    item {
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { navController.navigateDebugger(FontsPage) }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(DebuggerInfoItem(LocalContext.current.getString(R.string.appcues_debugger_info_fonts))) {
                InfoItemContent(rowScope = this@Row)
            }
        }

        DividerItem()
    }

    item {
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { navController.navigateDebugger(PluginsPage) }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(DebuggerInfoItem(LocalContext.current.getString(R.string.appcues_debugger_plugins_title))) {
                InfoItemContent(rowScope = this@Row)
            }
        }

        DividerItem()
    }

    item {
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { navController.navigateDebugger(LogsPage) }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            with(DebuggerInfoItem(LocalContext.current.getString(R.string.appcues_debugger_info_detailed_log))) {
                InfoItemContent(rowScope = this@Row)
            }
        }

        DividerItem()
    }
}

private fun LazyListScope.eventsSection(
    navController: NavHostController,
    currentFilter: EventType?,
    list: List<DebuggerEventItem>,
    onApplyFilter: (EventType?) -> Unit,
) {
    item {
        Box(
            modifier = Modifier
                .fillParentMaxWidth()
                .padding(12.dp),
        ) {
            val density = LocalDensity.current
            val isFilterExpanded = remember { mutableStateOf(false) }
            val filterImagePosition = remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }

            TextHeader(
                modifier = Modifier
                    .padding(start = 28.dp)
                    .align(Alignment.CenterStart),
                text = LocalContext.current.getString(R.string.appcues_debugger_recent_events_title)
            )

            Crossfade(
                targetState = currentFilter != null,
                modifier = Modifier.align(Alignment.CenterEnd),
                label = "filter cross-fade"
            ) {
                Icon(
                    modifier = Modifier
                        .clickable { isFilterExpanded.value = true }
                        .padding(12.dp)
                        .size(16.dp)
                        .onPlaced {
                            with(density) {
                                filterImagePosition.value = DpOffset(it.positionInRoot().x.toDp(), it.positionInRoot().y.toDp())
                            }
                        },
                    painter = painterResource(id = if (it) R.drawable.appcues_ic_filter_on else R.drawable.appcues_ic_filter_off),
                    contentDescription = LocalContext.current.getString(R.string.appcues_debugger_recent_events_filter_icon_description),
                    tint = if (it) LocalAppcuesTheme.current.brand else LocalAppcuesTheme.current.tertiary
                )
            }

            EventTypeFilterDropdown(isFilterExpanded, filterImagePosition, 36.dp, currentFilter) {
                onApplyFilter(it)
            }
        }
    }

    items(list) { item ->
        Row(
            modifier = Modifier
                .fillParentMaxWidth()
                .clickable { navController.navigateDebugger(EventDetailsPage.applyExtras(item)) }
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            item.EventItemIcon()

            item.EventItemContent(rowScope = this@Row)
        }

        DividerItem()
    }
}

@Composable
private fun EventTypeFilterDropdown(
    expanded: MutableState<Boolean>,
    filterPosition: MutableState<DpOffset>,
    filterButtonSize: Dp,
    filter: EventType?,
    onApplyFilter: (EventType?) -> Unit
) {
    val density = LocalDensity.current
    val filterDropdownWidth = remember { mutableStateOf(0.dp) }
    val currentExpanded = remember { mutableStateOf(false) }
    val targetExpanded = remember { expanded }
    val selectedFilter = remember { mutableStateOf(filter) }

    DropdownMenu(
        modifier = Modifier.onSizeChanged {
            with(density) { filterDropdownWidth.value = it.width.toDp() }
        },
        expanded = currentExpanded.value,
        offset = DpOffset(filterPosition.value.x - filterDropdownWidth.value, -(filterButtonSize / 2)),
        onDismissRequest = { targetExpanded.value = false },
    ) {
        // add a null object at the end to represent the option "All"
        EventType.values().toMutableList<EventType?>().apply { add(null) }.forEach {
            FilterEventTypeMenuItem(eventType = it, isSelected = selectedFilter.value == it) {
                targetExpanded.value = false
                selectedFilter.value = it
            }
        }
    }

    LaunchedEffect(targetExpanded.value) {
        if (targetExpanded.value) {
            currentExpanded.value = true
        } else {
            // we are adding a small delay when closing the dropdown so we can see the click
            // event going through
            delay(timeMillis = 200)
            currentExpanded.value = false
            // also postpone the apply of the filter here to look better when using it
            // it will apply the filter at the same time we are closing the dropdown
            onApplyFilter(selectedFilter.value)
        }
    }
}

@Composable
private fun ColumnScope.FilterEventTypeMenuItem(eventType: EventType?, isSelected: Boolean, onClick: () -> Unit) {
    val theme = LocalAppcuesTheme.current
    val background = animateColorAsState(
        targetValue = if (isSelected) theme.backgroundSelected else theme.background,
        label = "Dropdown item background"
    )
    val foreground = animateColorAsState(
        targetValue = if (isSelected) theme.brand else theme.primary,
        label = "Dropdown item foreground"
    )
    DropdownMenuItem(
        modifier = Modifier
            .align(Alignment.Start)
            .background(background.value),
        onClick = onClick,
    ) {
        Image(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .size(24.dp),
            painter = painterResource(id = eventType.toResourceId()),
            colorFilter = ColorFilter.tint(foreground.value),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_recent_events_item_icon_description),
            contentScale = ContentScale.None
        )

        Text(
            modifier = Modifier
                .width(160.dp)
                .padding(start = 20.dp),
            color = foreground.value,
            text = LocalContext.current.getString(eventType.getTitleString())
        )
    }
}
