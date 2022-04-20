package com.appcues.debugger.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.model.DebuggerStatusItem
import com.appcues.debugger.model.StatusType
import com.appcues.debugger.model.StatusType.ERROR
import com.appcues.debugger.model.StatusType.EXPERIENCE
import com.appcues.debugger.model.StatusType.LOADING
import com.appcues.debugger.model.StatusType.PHONE
import com.appcues.debugger.model.StatusType.SUCCESS
import com.appcues.debugger.model.TapActionType
import com.appcues.ui.theme.AppcuesColors

@Composable
internal fun DebuggerMain(debuggerViewModel: DebuggerViewModel) {
    val statusInfo = debuggerViewModel.statusInfo.collectAsState()

    LazyColumn {
        item {
            Box(
                modifier = Modifier.fillParentMaxWidth(),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = LocalContext.current.getString(R.string.debugger_status_title),
                    modifier = Modifier.padding(start = 40.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppcuesColors.HadfieldBlue,
                )
            }
        }

        items(statusInfo.value) { item ->
            StatusItemCompose(item = item) { debuggerViewModel.onStatusTapAction(it) }
        }
    }
}

@Composable
private fun LazyItemScope.StatusItemCompose(item: DebuggerStatusItem, onTap: (TapActionType) -> Unit) {
    Row(
        modifier = Modifier
            .fillParentMaxWidth()
            .then(
                if (item.tapActionType != null && item.statusType != LOADING)
                    Modifier.clickable { onTap(item.tapActionType) } else Modifier
            )
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        with(item) {
            Icon()

            Content(rowScope = this@Row)

            RefreshIcon()
        }
    }

    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = AppcuesColors.WhisperBlue,
        thickness = 1.dp,
    )
}

@Composable
private fun DebuggerStatusItem.Icon() {
    val iconModifier = Modifier
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .size(24.dp)

    if (statusType == LOADING) {
        CircularProgressIndicator(modifier = iconModifier, color = AppcuesColors.HadfieldBlue)
    } else {
        Image(
            painter = painterResource(id = statusType.toResourceId()),
            contentDescription = LocalContext.current.getString(R.string.debugger_status_item_icon_content_description, title),
            modifier = iconModifier,
            contentScale = ContentScale.None
        )
    }
}

@Composable
private fun DebuggerStatusItem.Content(rowScope: RowScope) {
    with(rowScope) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = AppcuesColors.Infinity
            )
            line1?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppcuesColors.OceanNight
                )
            }
            line2?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = AppcuesColors.OceanNight
                )
            }
        }
    }
}

@Composable
private fun DebuggerStatusItem.RefreshIcon() {
    if (showRefreshIcon && statusType != LOADING) {
        Image(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .size(24.dp),
            painter = painterResource(id = R.drawable.ic_reload),
            contentDescription = LocalContext.current.getString(R.string.debugger_status_reload_icon_content_description),
            contentScale = ContentScale.None
        )
    }
}

private fun StatusType.toResourceId(): Int {
    return when (this) {
        PHONE -> R.drawable.ic_mobile
        SUCCESS -> R.drawable.ic_success
        ERROR -> R.drawable.ic_error
        EXPERIENCE -> R.drawable.ic_experience
        // we never don't need loading icon
        LOADING -> 0
    }
}
