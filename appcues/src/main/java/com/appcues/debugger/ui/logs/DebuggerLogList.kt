package com.appcues.debugger.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.ui.ds.DividerItem
import com.appcues.debugger.ui.ds.FloatingBackButton
import com.appcues.debugger.ui.lazyColumnScrollIndicator
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.logging.LogMessage
import com.appcues.logging.LogType.DEBUG
import com.appcues.logging.LogType.ERROR
import com.appcues.logging.LogType.INFO
import com.appcues.logging.LogType.WARNING

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerLogList(
    viewModel: DebuggerViewModel,
    navController: NavHostController,
    onLogMessageSelected: (LogMessage) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val messages = viewModel.logMessages.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppcuesTheme.current.background)
            .lazyColumnScrollIndicator(lazyListState),
        state = lazyListState
    ) {
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }

        itemsIndexed(messages.value) { index, item ->
            ListItem(index, item, onLogMessageSelected)
        }

        item {
            Spacer(modifier = Modifier.padding(bottom = 16.dp))
        }
    }

    val firstVisible = remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val docked = firstVisible.value == 0 &&
        with(LocalDensity.current) { lazyListState.firstVisibleItemScrollOffset.toDp() < firstVisibleItemOffsetThreshold }

    FloatingBackButton(
        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
        docked = docked
    ) {
        navController.popBackStack()
    }
}

@Composable
private fun LazyItemScope.ListItem(index: Int, logMessage: LogMessage, onItemClicked: (LogMessage) -> Unit) {
    Row(
        modifier = Modifier
            .clickable { onItemClicked(logMessage) }
            .fillParentMaxWidth()
            .padding(horizontal = 20.dp)
            .testTag("log-message-$index"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp, horizontal = 20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            val theme = LocalAppcuesTheme.current
            val color = when (logMessage.type) {
                INFO, DEBUG -> theme.primary
                ERROR -> theme.error
                WARNING -> theme.warning
            }

            Text(
                text = stringResource(
                    id = R.string.appcues_debugger_item_title,
                    logMessage.type.displayName,
                    logMessage.timestamp.toLogFormat()
                ),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = color
            )
            Text(
                text = logMessage.message,
                fontSize = 12.sp,
                maxLines = 10,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.Monospace,
                color = color
            )
        }
    }

    DividerItem()
}
