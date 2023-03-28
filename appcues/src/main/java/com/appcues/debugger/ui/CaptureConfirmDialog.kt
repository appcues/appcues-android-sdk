package com.appcues.debugger.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.appcues.R.string
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.screencapture.Capture
import com.appcues.ui.extensions.xShapePath
import com.appcues.ui.theme.AppcuesColors

@Composable
internal fun CaptureConfirmDialog(capture: Capture, debuggerState: MutableDebuggerState, debuggerViewModel: DebuggerViewModel) {
    val text = remember { mutableStateOf(capture.displayName) }

    // don't show if current debugger is paused
    if (debuggerState.isPaused.value) return

    Dialog(
        onDismissRequest = {
            debuggerViewModel.closeExpandedView()
        },
    ) {
        Column(
            modifier = Modifier
                .background(AppcuesColors.DebuggerBackground, RoundedCornerShape(6.dp))
                .padding(9.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Header(debuggerViewModel = debuggerViewModel)
            CaptureContents(debuggerViewModel = debuggerViewModel, capture = capture, text = text)
        }
    }
}

@Composable
private fun Header(debuggerViewModel: DebuggerViewModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.padding(start = 16.dp),
            text = stringResource(id = string.appcues_screen_capture_title),
            style = TextStyle(
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.weight(1.0f))
        Spacer(
            modifier = Modifier
                .size(48.dp, 48.dp)
                .clickable(
                    onClick = { debuggerViewModel.closeExpandedView() },
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false, 24.dp),
                    onClickLabel = stringResource(id = string.appcues_screen_capture_dismiss)
                )
                .drawBehind {
                    xShapePath(color = Color.Black, pathSize = 16.dp, strokeWidth = 1.5.dp)
                        .also { drawPath(path = it, Color.Black) }
                }
        )
    }
}

@Composable
private fun CaptureContents(debuggerViewModel: DebuggerViewModel, capture: Capture, text: MutableState<String>) {
    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = rememberAsyncImagePainter(capture.screenshot),
            contentDescription = stringResource(id = string.appcues_screen_capture_image_description),
            modifier = Modifier
                .height(375.dp)
                .border(1.dp, AppcuesColors.CaptureImageBorder),
            contentScale = ContentScale.Fit,
        )
        OutlinedTextField(
            value = text.value,
            singleLine = true,
            onValueChange = { text.value = it },
            label = { Text(text = stringResource(id = string.appcues_screen_capture_text_input_label)) },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color.Transparent,
                focusedBorderColor = AppcuesColors.CaptureTextInputBorder,
                focusedLabelColor = AppcuesColors.Blurple,
                unfocusedBorderColor = AppcuesColors.CaptureTextInputBorder,
                unfocusedLabelColor = AppcuesColors.Blurple,
            )
        )
        Row {
            CaptureButton(
                modifier = Modifier
                    .height(40.dp)
                    .background(AppcuesColors.DebuggerBackground)
                    .border(1.dp, AppcuesColors.Blurple, RoundedCornerShape(6.dp))
                    .clickable { debuggerViewModel.closeExpandedView() },
                text = stringResource(id = string.appcues_screen_capture_cancel),
                textColor = AppcuesColors.Blurple,
            )
            Spacer(modifier = Modifier.weight(1.0f))
            CaptureButton(
                modifier = Modifier
                    .height(40.dp)
                    .background(
                        Brush.horizontalGradient(listOf(AppcuesColors.Blurple, AppcuesColors.CaptureButtonGradientEnd)),
                        RoundedCornerShape(6.dp)
                    )
                    .conditionalClickable(
                        enabled = text.value.isEmpty().not(),
                        onClick = {
                            val updatedCapture = capture.copy(displayName = text.value).apply { screenshot = capture.screenshot }
                            debuggerViewModel.onScreenCaptureConfirm(updatedCapture)
                        }
                    ),
                text = stringResource(id = string.appcues_screen_capture_ok),
                textColor = Color.White,
            )
        }
    }
}

private fun Modifier.conditionalClickable(enabled: Boolean, onClick: () -> Unit) = composed {
    this.then(
        if (enabled) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier.alpha(ContentAlpha.disabled)
        }
    )
}

@Composable
private fun CaptureButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color,
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = text,
                style = TextStyle(color = textColor)
            )
        }
    }
}
