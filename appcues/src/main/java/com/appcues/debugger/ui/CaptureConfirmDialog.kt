package com.appcues.debugger.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.appcues.R.string
import com.appcues.ViewElement
import com.appcues.debugger.DebuggerViewModel
import com.appcues.debugger.screencapture.model.Capture
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
                    xShapePath(pathSize = 16.dp).also {
                        drawPath(
                            path = it,
                            color = Color.Black,
                            style = Stroke(1.5.dp.toPx()),
                        )
                    }
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
        BoxWithConstraints(
            modifier = Modifier.heightIn(max = 375.dp)
        ) {
            // Captured screenshot
            Image(
                bitmap = capture.screenshot.bitmap.asImageBitmap(),
                modifier = Modifier.border(1.dp, AppcuesColors.CaptureImageBorder),
                contentDescription = stringResource(id = string.appcues_screen_capture_image_description),
                contentScale = ContentScale.Fit,
            )

            // Overlay to highlight targetable elements
            Canvas(modifier = Modifier.matchParentSize()) {
                // figure out the scale value
                val landscape = capture.layout.width > capture.layout.height
                val scale = if (landscape) {
                    maxWidth / capture.layout.width.dp
                } else {
                    maxHeight / capture.layout.height.dp
                }

                drawTargetableElement(capture.layout, scale)
            }
        }

        if (capture.targetableElementCount > 0) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = string.appcues_screen_capture_not_seeing_element),
                fontSize = 12.sp
            )
        } else {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = string.appcues_screen_capture_no_element),
                fontSize = 12.sp,
                color = Color(color = 0xFFF39325)
            )
        }

        TextWebLink(
            text = stringResource(id = string.appcues_screen_capture_troubleshoot),
            url = "https://docs.appcues.com/mobile-sdk-screen-capture-help"
        )

        OutlinedTextField(
            modifier = Modifier.testTag("screen-capture-name"),
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
                        enabled = text.value
                            .isEmpty()
                            .not(),
                        onClick = {
                            val updatedCapture = capture.copy(displayName = text.value)
                            debuggerViewModel.onScreenCaptureConfirm(updatedCapture)
                        }
                    ),
                text = stringResource(id = string.appcues_screen_capture_ok),
                textColor = Color.White,
            )
        }
    }
}

@Composable
private fun TextWebLink(text: String, @Suppress("SameParameterValue") url: String) {
    val uriHandler = LocalUriHandler.current
    ClickableText(
        modifier = Modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            append(text)
            addStyle(
                style = SpanStyle(
                    color = Color(color = 0xFF0A7AEA),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                ),
                start = 0,
                end = text.length
            )
        },
        onClick = { uriHandler.openUri(url) }
    )
}

private fun DrawScope.drawTargetableElement(element: ViewElement, scale: Float) {
    val x = element.x.dp.toPx() * scale
    val y = element.y.dp.toPx() * scale

    // only draw if this element is targetable
    if (element.selector != null) {
        val width = element.width.dp.toPx() * scale
        val height = element.height.dp.toPx() * scale

        drawRect(
            topLeft = Offset(x, y),
            size = Size(width, height),
            color = Color(color = 0x80E3F2FF),
        )

        drawRect(
            topLeft = Offset(x, y),
            size = Size(width, height),
            color = Color(color = 0xFF1491FF),
            style = Stroke(width = 1.dp.toPx()),
        )
    }

    element.children?.forEach { drawTargetableElement(it, scale) }
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
