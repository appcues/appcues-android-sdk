package com.appcues.ui.primitive

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.styling.ComponentColor
import com.appcues.data.model.styling.ComponentDataType
import com.appcues.data.model.styling.ComponentDataType.ADDRESS
import com.appcues.data.model.styling.ComponentDataType.EMAIL
import com.appcues.data.model.styling.ComponentDataType.NAME
import com.appcues.data.model.styling.ComponentDataType.NUMBER
import com.appcues.data.model.styling.ComponentDataType.PHONE
import com.appcues.data.model.styling.ComponentDataType.TEXT
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.composables.LocalExperienceStepFormStateDelegate
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.checkErrorStyle
import com.appcues.ui.extensions.getColor
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.getMargins
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.styleBackground
import com.appcues.ui.extensions.styleBorder
import com.appcues.ui.extensions.styleCorner
import com.appcues.ui.extensions.styleShadow
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import com.appcues.ui.utils.margin
import java.util.UUID
import kotlin.math.max

// constants used in determining the height of the text input box based on the text properties in the model

// multiplier that provides the proper input line height based on the font size used in TextField
private const val FONT_SIZE_HEIGHT_MULTIPLIER = 1.3f

// multiplier that provides the proper input line height based on the line height used in TextField
private const val LINE_HEIGHT_MULTIPLIER = 1.3f

// default top and bottom padding for a TextField
private const val TEXT_INPUT_PADDING = 16.0

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun TextInputPrimitive.Compose(modifier: Modifier) {

    val formState = LocalExperienceStepFormStateDelegate.current.apply {
        register(this@Compose)
    }
    val showError = formState.shouldShowError(this)
    val errorTint = if (showError) errorLabel?.style?.foregroundColor else null
    val isDark = isSystemInDarkTheme()
    val textStyle = LocalTextStyle.current.applyStyle(
        style = textFieldStyle,
        context = LocalContext.current,
        isDark = isDark,
    )

    // this avoids recalculating the label style unless the showError state changes
    val updatedLabel = remember(showError) {
        label.checkErrorStyle(showError, errorLabel)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = style.getHorizontalAlignment(),
    ) {
        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val layoutDirection = LocalLayoutDirection.current
        val lastItemId = rememberUpdatedState(newValue = formState.lastTextFocusableItem)
        val isLastInputItem = derivedStateOf { lastItemId.value?.let { it.id == id } ?: false }

        updatedLabel.Compose()

        // Several styling customization options for TextField noted here https://stackoverflow.com/a/68592613
        TextField(
            value = formState.getValue(this@Compose),
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    formState.setValue(this@Compose, it)
                }
            },
            modifier = Modifier
                .margin(textFieldStyle.getMargins())
                .styleShadow(textFieldStyle, isDark)
                .fillMaxWidth()
                .styleInputBoxHeight(textStyle, numberOfLines)
                .styleCorner(textFieldStyle)
                .styleTintedBorder(errorTint, textFieldStyle, isDark)
                .styleBackground(textFieldStyle, isDark)
                .padding(textFieldStyle.getPaddings()),
            textStyle = textStyle,
            shape = RoundedCornerShape(8.dp),
            maxLines = numberOfLines,
            singleLine = numberOfLines == 1,
            placeholder = placeholder?.let {
                {
                    it.Compose()
                }
            },
            keyboardOptions = getKeyboardOptions(isLastInputItem),
            keyboardActions = getKeyboardActions(layoutDirection, focusManager, keyboardController),
            colors = getColors(isDark),
        )

        if (showError) {
            errorLabel?.Compose()
        }
    }
}

@Composable
private fun TextInputPrimitive.getColors(isDark: Boolean) =
    TextFieldDefaults.textFieldColors(
        backgroundColor = Color.Transparent,
        // the builder should always send this value, but default to the theme like the standard default behavior
        cursorColor = cursorColor?.getColor(isDark) ?: MaterialTheme.colors.primary,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
    )

@Composable
private fun TextInputPrimitive.getKeyboardOptions(isLastInputItem: State<Boolean>) =
    KeyboardOptions(
        keyboardType = mapKeyboardType(dataType),
        imeAction = if (isLastInputItem.value) ImeAction.Done else ImeAction.Next,
    )

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun getKeyboardActions(
    layoutDirection: LayoutDirection,
    focusManager: FocusManager,
    keyboardController: SoftwareKeyboardController?
) = KeyboardActions(
    onNext = {
        val movedSideways = when (layoutDirection) {
            // if layout direction is left to right we try to move to the right
            Ltr -> focusManager.moveFocus(FocusDirection.Right)
            // if layout direction is right to left we try to move to the left
            Rtl -> focusManager.moveFocus(FocusDirection.Left)
        }

        // move focus to next element below
        if (!movedSideways) focusManager.moveFocus(FocusDirection.Down)
    },
    onDone = {
        // closes keyboard
        keyboardController?.hide()
        // clear focus // with talkback on this takes the cursor of the textField
        // while its still highlighted
        focusManager.clearFocus()
    }
)

// wrapper that will apply an error tint color, if applicable
// otherwise, just fall back to the normal styleBorder modifier
private fun Modifier.styleTintedBorder(
    tintColor: ComponentColor?,
    style: ComponentStyle,
    isDark: Boolean
) = this.then(
    if (tintColor != null) {
        val borderWidth = max(style.borderWidth ?: 0.0, 1.0).dp
        Modifier.border(borderWidth, tintColor.getColor(isDark), RoundedCornerShape(style.cornerRadius.dp))
    } else {
        Modifier.styleBorder(style, isDark)
    }
)

private fun Modifier.styleInputBoxHeight(textStyle: TextStyle, numberOfLines: Int) = this.then(
    with(textStyle) {
        // the first line is always based on the font size, not any additional line height
        val firstLine: Float = fontSize.value * FONT_SIZE_HEIGHT_MULTIPLIER

        // other lines are determined by line height, if exists, or by font size
        val additionalLineHeight: Float = if (lineHeight.isUnspecified) {
            firstLine
        } else {
            lineHeight.value * LINE_HEIGHT_MULTIPLIER
        }

        // the TextField includes 16dp padding at top and bottom for touch target
        val padding = TEXT_INPUT_PADDING * 2

        val height = (firstLine + (additionalLineHeight * (numberOfLines - 1)) + padding).dp

        // using min height here so that additional padding in the data model can also be applied
        Modifier.defaultMinSize(minHeight = height)
    }
)

private fun mapKeyboardType(value: ComponentDataType): KeyboardType = when (value) {
    TEXT -> KeyboardType.Text
    NUMBER -> KeyboardType.Number
    EMAIL -> KeyboardType.Email
    PHONE -> KeyboardType.Phone
    NAME -> KeyboardType.Text
    ADDRESS -> KeyboardType.Text
}

private val textPrimitive = TextPrimitive(
    id = UUID.randomUUID(),
    text = "Enter a value",
)

private val textInputPrimitive = TextInputPrimitive(
    id = UUID.randomUUID(),
    label = textPrimitive,
    defaultValue = "text input",
)

@Composable
@Preview(name = "textInput default", group = "basic")
internal fun PreviewTestInputDefault() {
    AppcuesPreviewPrimitive {
        textInputPrimitive
    }
}

@Composable
@Preview(name = "textInput multiline", group = "basic")
internal fun PreviewTestInputMultiline() {
    AppcuesPreviewPrimitive {
        textInputPrimitive.copy(
            numberOfLines = 3,
            defaultValue = "here is\nsome text that\ngoes multi line\nover limit"
        )
    }
}

@Composable
@Preview(name = "textInput foregroundColor", group = "properties")
internal fun PreviewTextInputComponentColor() {
    val previewPrimitive = textInputPrimitive.copy(
        textFieldStyle = textInputPrimitive.textFieldStyle.copy(
            foregroundColor = ComponentColor(light = 0xFF000000, dark = 0xFFFFFFFF)
        ),
        label = textPrimitive.copy(
            style = textPrimitive.style.copy(
                foregroundColor = ComponentColor(light = 0xFF101010, dark = 0xFF808080)
            )
        )
    )

    Column {
        AppcuesPreviewPrimitive(isDark = true) {
            previewPrimitive
        }
        AppcuesPreviewPrimitive(isDark = false) {
            previewPrimitive
        }
    }
}
