package com.appcues.debugger.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.StatusType
import com.appcues.debugger.ui.theme.AppcuesThemeColors
import com.appcues.debugger.ui.theme.LocalAppcuesTheme

@Composable
internal fun DebuggerStatusItem.StatusItemIcon() {
    val iconModifier = Modifier
        .padding(start = 20.dp)
        .padding(24.dp)
        .size(24.dp)
    val theme = LocalAppcuesTheme.current

    if (statusType == StatusType.LOADING) {
        CircularProgressIndicator(modifier = iconModifier, color = theme.primary)
    } else {
        Image(
            painter = painterResource(id = statusType.toResourceId()),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_status_item_icon_content_description, title),
            modifier = iconModifier,
            contentScale = ContentScale.None,
            colorFilter = statusType.getColorFilter(theme)
        )
    }
}

private fun StatusType.toResourceId(): Int {
    return when (this) {
        StatusType.PHONE -> R.drawable.appcues_ic_mobile
        StatusType.SUCCESS -> R.drawable.appcues_ic_success
        StatusType.ERROR -> R.drawable.appcues_ic_error
        StatusType.EXPERIENCE -> R.drawable.appcues_ic_experience
        StatusType.UNKNOWN -> R.drawable.appcues_ic_unknown
        // we never need loading icon
        StatusType.LOADING -> 0
    }
}

private fun StatusType.getColorFilter(theme: AppcuesThemeColors): ColorFilter? {
    return when (this) {
        StatusType.PHONE, StatusType.EXPERIENCE, StatusType.UNKNOWN -> ColorFilter.tint(theme.primary)
        else -> null
    }
}

@Composable
internal fun DebuggerStatusItem.StatusItemContent(rowScope: RowScope) {
    with(rowScope) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = LocalAppcuesTheme.current.primary
            )
            line1?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalAppcuesTheme.current.secondary
                )
            }
            line2?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = LocalAppcuesTheme.current.secondary
                )
            }
        }
    }
}

@Composable
internal fun DebuggerStatusItem.RefreshIcon() {
    if (showRefreshIcon && statusType != StatusType.LOADING) {
        Icon(
            modifier = Modifier
                .padding(24.dp)
                .size(16.dp),
            painter = painterResource(id = R.drawable.appcues_ic_reload),
            contentDescription = LocalContext.current.getString(R.string.appcues_debugger_status_reload_icon_content_description),
            tint = LocalAppcuesTheme.current.secondary
        )
    }
}
