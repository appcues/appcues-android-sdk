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
import com.appcues.ui.theme.AppcuesColors

@Composable
internal fun DebuggerEventItem.EventItemIcon() {
    val iconModifier = Modifier
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .size(24.dp)

    Image(
        painter = painterResource(id = type.toResourceId()),
        contentDescription = LocalContext.current.getString(R.string.debugger_recent_events_item_icon_content_description),
        modifier = iconModifier,
        contentScale = ContentScale.None
    )
}

@Composable
internal fun DebuggerEventItem.EventItemContent(rowScope: RowScope) {
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
                    painter = painterResource(id = R.drawable.ic_clock),
                    contentDescription = LocalContext.current.getString(R.string.debugger_recent_events_timestamp_icon_content_description),
                    contentScale = ContentScale.Fit
                )
                Text(
                    text = timestamp,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppcuesColors.OceanNight
                )
            }
        }
    }
}
