package com.appcues.debugger.ui.details

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.getTitleString
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.ui.theme.AppcuesColors
import java.sql.Timestamp

private val FIRST_VISIBLE_ITEM_OFFSET_THRESHOLD = 56.dp

@Composable
internal fun DebuggerEventDetails(debuggerEventItem: DebuggerEventItem?, onBackPressed: () -> Unit) {
    if (debuggerEventItem == null) return

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
            if (it.properties != null && it.properties.isNotEmpty()) {
                propertiesTitle(it.title)
                properties(it.properties)
            }
        }
    }

    val keepBackButtonDocked = lazyListState.firstVisibleItemIndex == 0 &&
        with(LocalDensity.current) { lazyListState.firstVisibleItemScrollOffset.toDp() < FIRST_VISIBLE_ITEM_OFFSET_THRESHOLD }

    val elevation = animateDpAsState(if (keepBackButtonDocked) 0.dp else 12.dp)

    Box(
        modifier = Modifier
            .padding(top = 12.dp, start = 8.dp)
            .size(48.dp)
            .clickable { onBackPressed() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .shadow(elevation.value, RoundedCornerShape(percent = 100))
                .clip(RoundedCornerShape(percent = 100))
                .size(32.dp)
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.appcues_ic_back),
                contentDescription = LocalContext.current.getString(R.string.appcues_debugger_back_description)
            )
        }
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
