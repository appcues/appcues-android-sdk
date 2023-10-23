package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.R.string
import com.appcues.debugger.model.DebuggerToast.ScreenCaptureFailure
import com.appcues.debugger.model.DebuggerToast.ScreenCaptureSuccess
import com.appcues.ui.theme.AppcuesColors
import kotlinx.coroutines.delay

private const val SUCCESS_TOAST_LENGTH = 3_000L
private const val FAILURE_TOAST_LENGTH = 6_000L

@Composable
internal fun BoxScope.ToastView(debuggerState: MutableDebuggerState) {

    when (val toastState = debuggerState.toast.targetState) {
        is ScreenCaptureSuccess -> SuccessToast(toast = toastState, debuggerState = debuggerState)
        is ScreenCaptureFailure -> FailureToast(toast = toastState, debuggerState = debuggerState)
        null -> Unit
    }
}

@Composable
internal fun BoxScope.SuccessToast(toast: ScreenCaptureSuccess, debuggerState: MutableDebuggerState) {
    // any tap on this background will dismiss the toast before it auto expires
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = toast.onDismiss
            )
            .testTag("capture-success-toast")
    )

    AnimatedVisibility(
        visible = debuggerState.toast.targetState != null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .background(color = AppcuesColors.DebuggerToastSuccessBackground, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    ) {
                        append("\"${toast.capture.displayName}\" ")
                    }
                    withStyle(
                        style = SpanStyle(fontWeight = FontWeight.Normal, color = Color.White, fontSize = 14.sp)
                    ) {
                        append(stringResource(id = string.appcues_screen_capture_toast_success_suffix))
                    }
                },
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
            )
        }
        LaunchedEffect(debuggerState.toast.targetState) {
            delay(timeMillis = SUCCESS_TOAST_LENGTH)
            toast.onDismiss()
        }
    }
}
@Composable
internal fun BoxScope.FailureToast(toast: ScreenCaptureFailure, debuggerState: MutableDebuggerState) {
    // any tap on this background will dismiss the toast before it auto expires
    Spacer(
        modifier = Modifier
            .matchParentSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = toast.onDismiss
            )
            .testTag("capture-failure-toast")
    )

    AnimatedVisibility(
        visible = debuggerState.toast.targetState != null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier
                .background(color = AppcuesColors.DebuggerToastFailureBackground, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = string.appcues_screen_capture_toast_upload_failed),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.weight(1.0f))

            Box(
                modifier = Modifier
                    .height(40.dp)
                    .border(
                        width = 1.dp,
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White
                    )
                    .clickable(onClick = toast.onRetry)
            ) {
                Text(
                    text = stringResource(id = string.appcues_screen_capture_toast_try_again),
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(start = 16.dp, end = 16.dp)
                )
            }
        }
        LaunchedEffect(debuggerState.toast.targetState) {
            delay(timeMillis = FAILURE_TOAST_LENGTH)
            toast.onDismiss()
        }
    }
}
