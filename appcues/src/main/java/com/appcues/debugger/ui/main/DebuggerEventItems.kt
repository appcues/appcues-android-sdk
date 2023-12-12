package com.appcues.debugger.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.ds.TextPrimary
import com.appcues.debugger.ui.ds.TextSecondary
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.debugger.ui.toResourceId
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DebuggerEventItem.EventItemIcon() {
    val iconModifier = Modifier
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .size(24.dp)

    val theme = LocalAppcuesTheme.current

    Image(
        painter = painterResource(id = type.toResourceId()),
        contentDescription = LocalContext.current.getString(R.string.appcues_debugger_recent_events_item_icon_description),
        modifier = iconModifier,
        contentScale = ContentScale.None,
        colorFilter = ColorFilter.tint(theme.primary)
    )
}

private const val EVENT_DATE_FORMAT = "hh:mm:ss"

@Composable
internal fun DebuggerEventItem.EventItemContent(rowScope: RowScope) {
    val dateFormat = SimpleDateFormat(EVENT_DATE_FORMAT, Locale.getDefault())
    with(rowScope) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            TextPrimary(text = name)

            Row(
                Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(12.dp),
                    painter = painterResource(id = R.drawable.appcues_ic_clock),
                    contentDescription = LocalContext.current.getString(R.string.appcues_debugger_recent_events_timestamp_icon_description),
                    contentScale = ContentScale.Fit
                )

                TextSecondary(text = dateFormat.format(Date(timestamp)))
            }
        }
    }
}
