package com.appcues.debugger.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.R.string
import com.appcues.debugger.ui.icons.Close
import com.appcues.debugger.ui.icons.DebuggerIcons
import com.appcues.debugger.ui.theme.LocalAppcuesTheme
import kotlinx.coroutines.delay

@Composable
internal fun AppcuesSearchView(
    modifier: Modifier,
    height: Dp,
    elevation: Dp = 0.dp,
    hint: String,
    inputDelay: Long = 0,
    onInput: (String) -> Unit
) {
    val firstComposition = remember { mutableStateOf(true) }
    val cornerDp = remember { derivedStateOf { height / 2 } }
    val focusRequester = remember { FocusRequester() }
    val isFocusOn = remember { mutableStateOf(false) }
    val border = animateColorAsState(
        targetValue = if (isFocusOn.value) LocalAppcuesTheme.current.inputActive else LocalAppcuesTheme.current.input,
        label = "SearchView border"
    )

    Box(
        modifier = Modifier
            .then(modifier)
            .shadow(elevation, RoundedCornerShape(cornerDp.value))
            .clip(RoundedCornerShape(cornerDp.value))
            .border(1.dp, border.value, RoundedCornerShape(cornerDp.value))
            .background(LocalAppcuesTheme.current.background)
            .height(height)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { focusRequester.requestFocus() },
    ) {
        val text = remember { mutableStateOf(TextFieldValue(String())) }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current

        BasicTextField(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .matchParentSize()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .padding(end = 20.dp)
                .focusRequester(focusRequester)
                .onFocusChanged {
                    keyboardController?.show()
                    isFocusOn.value = it.isFocused
                },
            value = text.value,
            onValueChange = {
                if (isFocusOn.value) {
                    text.value = it
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { keyboardController?.hide() }
            ),
            textStyle = TextStyle.Default.copy(color = LocalAppcuesTheme.current.primary),
            cursorBrush = SolidColor(LocalAppcuesTheme.current.inputActive)
        )

        SearchViewOverlay(isFocusOn, height, keyboardController, focusManager, hint) { text.value = TextFieldValue(String()) }

        LaunchedEffect(text.value) {
            if (firstComposition.value) {
                firstComposition.value = false
                return@LaunchedEffect
            }

            if (text.value.text.isEmpty()) {
                onInput(String())
            } else {
                delay(inputDelay)
                onInput(text.value.text)
            }
        }
    }
}

@Composable
private fun BoxScope.SearchViewOverlay(
    isFocusOn: MutableState<Boolean>,
    height: Dp,
    keyboardController: SoftwareKeyboardController?,
    focusManager: FocusManager,
    hint: String,
    onClear: () -> Unit,
) {
    if (isFocusOn.value) {
        Icon(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(height)
                .clip(CircleShape)
                .clickable {
                    onClear()
                    isFocusOn.value = false
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
                .padding(8.dp),
            imageVector = DebuggerIcons.Filled.Close,
            contentDescription = LocalContext.current.getString(string.appcues_debugger_font_details_clean_filter),
            tint = LocalAppcuesTheme.current.secondary
        )
    } else {
        Text(
            text = hint,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
        )
    }
}
