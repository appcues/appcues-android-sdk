package com.appcues.ui.debugger

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R
import com.appcues.ui.debugger.StatusType.ERROR
import com.appcues.ui.debugger.StatusType.EXPERIENCE
import com.appcues.ui.debugger.StatusType.LOADING
import com.appcues.ui.debugger.StatusType.PHONE
import com.appcues.ui.debugger.StatusType.SUCCESS

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
                    text = "Status",
                    modifier = Modifier.padding(start = 40.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(color = 0xFF0D7EF9)
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
        color = Color(color = 0xFFE7ECF3),
        thickness = 1.dp,
    )
}

@Composable
private fun DebuggerStatusItem.Icon() {
    val iconModifier = Modifier
        .padding(horizontal = 24.dp, vertical = 20.dp)
        .size(24.dp)

    if (statusType == LOADING) {
        CircularProgressIndicator(modifier = iconModifier, color = Color(color = 0xFF0D7EF9))
    } else {
        Image(
            painter = painterResource(id = statusType.toResourceId()),
            contentDescription = "$title icon",
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
                color = Color(color = 0xFF242A35)
            )
            line1?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(color = 0xFF627293)
                )
            }
            line2?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(color = 0xFF627293)
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
            contentDescription = "reload",
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
