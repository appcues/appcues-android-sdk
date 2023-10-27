package com.appcues.debugger.ui.events

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.getTitleString
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.shared.FloatingBackButton
import com.appcues.ui.theme.AppcuesColors
import java.sql.Timestamp

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerEventDetails(debuggerEventItem: DebuggerEventItem, navController: NavHostController) {
    val lazyListState = rememberLazyListState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppcuesColors.DebuggerBackground)
            .lazyColumnScrollIndicator(lazyListState),
        state = lazyListState
    ) {
        detailsTitle()

        details(debuggerEventItem)

        debuggerEventItem.propertySections.forEach {
            if (!it.properties.isNullOrEmpty()) {
                propertiesTitle(it.title)
                properties(it.properties)
            }
        }
    }

    val isFirstVisible = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val isOverThreshold = with(LocalDensity.current) {
        lazyListState.firstVisibleItemScrollOffset.toDp() < firstVisibleItemOffsetThreshold
    }
    val docked = isFirstVisible.value == 0 && isOverThreshold

    FloatingBackButton(
        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
        docked = docked
    ) {
        navController.popBackStack()
    }
}

private fun LazyListScope.detailsTitle() {
    item {
        Spacer(modifier = Modifier.height(80.dp))
    }

    item {
        Text(
            text = LocalContext.current.getString(R.string.appcues_debugger_event_details_title),
            modifier = Modifier.padding(start = 40.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppcuesColors.HadfieldBlue,
        )
    }
}

private fun LazyListScope.details(event: DebuggerEventItem) {
    item {
        val context = LocalContext.current

        ListItem(
            key = context.getString(R.string.appcues_debugger_event_details_type_title),
            value = context.getString(event.type.getTitleString())
        )

        ListItem(
            key = context.getString(R.string.appcues_debugger_event_details_name_title),
            value = event.name
        )

        ListItem(
            key = context.getString(R.string.appcues_debugger_event_details_timestamp_title),
            value = Timestamp(event.timestamp).toString()
        )
    }
}

private fun LazyListScope.propertiesTitle(title: String) {
    item {
        Text(
            text = title,
            modifier = Modifier.padding(start = 40.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppcuesColors.HadfieldBlue,
        )
    }
}

private fun LazyListScope.properties(properties: List<Pair<String, Any?>>) {
    items(properties.toList()) { item ->
        ListItem(key = item.first, value = item.second?.toString() ?: "")
    }

    item {
        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }
}

@Composable
private fun LazyItemScope.ListItem(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = key,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppcuesColors.Infinity
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = AppcuesColors.SharkbaitOhAh
            )
        }
    }

    ListItemDivider()
}

@Composable
private fun ListItemDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppcuesColors.WhisperBlue,
        thickness = 1.dp,
    )
}
