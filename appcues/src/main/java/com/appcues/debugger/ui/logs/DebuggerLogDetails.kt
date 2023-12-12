@file:JvmName("DebuggerLogDetailsKt")

package com.appcues.debugger.ui.logs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.appcues.R
import com.appcues.debugger.ui.shared.FloatingBackButton
import com.appcues.debugger.ui.shared.copyToClipboardAndToast
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import com.appcues.logging.LogMessage
import com.appcues.logging.LogType.DEBUG
import com.appcues.logging.LogType.ERROR
import com.appcues.logging.LogType.INFO

private val firstVisibleItemOffsetThreshold = 56.dp

@Composable
internal fun DebuggerLogDetails(message: LogMessage, navController: NavHostController) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val color = when (message.type) {
        INFO, DEBUG -> LocalAppcuesTheme.current.primary
        ERROR -> LocalAppcuesTheme.current.error
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalAppcuesTheme.current.background)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        val text = stringResource(id = R.string.appcues_debugger_log_copy_log)
        TextButton(
            modifier = Modifier
                .semantics {
                    this.contentDescription = text
                }
                .align(Alignment.End),
            onClick = { copyToClipboardAndToast(context, clipboard, message.message) }
        ) {
            Text(
                text = text,
                color = LocalAppcuesTheme.current.link
            )
        }

        Text(
            text = stringResource(id = R.string.appcues_debugger_log_level, message.type.displayName),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
        Text(
            text = stringResource(id = R.string.appcues_debugger_log_timestamp, message.timestamp.toLogFormat()),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = color
        )
        Divider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = LocalAppcuesTheme.current.divider,
            thickness = 1.dp,
        )
        Text(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            softWrap = false,
            text = message.message,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = FontFamily.Monospace,
            color = color
        )

        Spacer(modifier = Modifier.padding(bottom = 16.dp))
    }

    val docked = with(LocalDensity.current) { scrollState.value.toDp() < firstVisibleItemOffsetThreshold }

    FloatingBackButton(
        modifier = Modifier.padding(top = 12.dp, start = 8.dp),
        docked = docked
    ) {
        navController.popBackStack()
    }
}
