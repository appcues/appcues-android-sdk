package com.appcues.debugger.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.getTitleString
import com.appcues.ui.theme.AppcuesColors
import java.sql.Timestamp

@Composable
internal fun DebuggerEventDetails(debuggerEventItem: DebuggerEventItem?, onBackPressed: () -> Unit) {
    if (debuggerEventItem == null) return

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        backButton(onBackPressed)

        detailsTitle()

        details(debuggerEventItem)

        if (debuggerEventItem.properties != null && debuggerEventItem.properties.isNotEmpty()) {
            propertiesTitle()

            properties(debuggerEventItem.properties)
        }

        if (debuggerEventItem.identityProperties != null && debuggerEventItem.identityProperties.isNotEmpty()) {
            identityPropertiesTitle()

            properties(debuggerEventItem.identityProperties)
        }
    }
}

private fun LazyListScope.backButton(onBackPressed: () -> Unit) {
    item {
        IconButton(
            modifier = Modifier.padding(top = 24.dp, start = 12.dp),
            onClick = onBackPressed
        ) {
            Icon(
                painter = painterResource(id = R.drawable.appcues_ic_back),
                contentDescription = LocalContext.current.getString(R.string.debugger_event_details_back_content_description)
            )
        }
    }
}

private fun LazyListScope.detailsTitle() {
    item {
        Text(
            text = LocalContext.current.getString(R.string.debugger_event_details_title),
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
            key = context.getString(R.string.debugger_event_details_type_title),
            value = context.getString(event.type.getTitleString())
        )

        ListItem(
            key = context.getString(R.string.debugger_event_details_name_title),
            value = event.name
        )

        ListItem(
            key = context.getString(R.string.debugger_event_details_timestamp_title),
            value = Timestamp(event.timestamp).toString()
        )
    }
}

private fun LazyListScope.propertiesTitle() {
    item {
        Text(
            text = LocalContext.current.getString(R.string.debugger_event_details_properties_title),
            modifier = Modifier.padding(start = 40.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppcuesColors.HadfieldBlue,
        )
    }
}

private fun LazyListScope.identityPropertiesTitle() {
    item {
        Text(
            text = LocalContext.current.getString(R.string.debugger_event_details_identity_auto_properties_title),
            modifier = Modifier.padding(start = 40.dp, top = 20.dp, bottom = 16.dp),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppcuesColors.HadfieldBlue,
        )
    }
}

private fun LazyListScope.properties(properties: List<Pair<String, Any>>) {
    items(properties.toList()) { item ->
        ListItem(key = item.first, value = item.second.toString())
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
                color = AppcuesColors.OceanNight
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
