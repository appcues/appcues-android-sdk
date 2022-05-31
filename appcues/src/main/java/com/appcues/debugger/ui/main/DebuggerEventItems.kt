package com.appcues.debugger.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.debugger.model.DebuggerEventItem
import com.appcues.debugger.ui.toResourceId
import com.appcues.ui.theme.AppcuesColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DebuggerEventItem.EventItemIcon() {
    val iconModifier = Modifier
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .size(24.dp)

    Image(
        painter = painterResource(id = type.toResourceId()),
        contentDescription = LocalContext.current.getString(R.string.appcues_debugger_recent_events_item_icon_description),
        modifier = iconModifier,
        contentScale = ContentScale.None
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
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppcuesColors.Infinity
            )

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
                Text(
                    text = dateFormat.format(Date(timestamp)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppcuesColors.SharkbaitOhAh
                )
            }
        }
    }
}
