package com.appcues.ui.primitive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import com.appcues.ui.ExperienceStepFormItem.SingleTextFormItem
import com.appcues.ui.LocalExperienceStepFormStateDelegate
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.theme.AppcuesPreviewPrimitive
import java.util.UUID

@Composable
internal fun TextInputPrimitive.Compose(modifier: Modifier) {

    val formState = LocalExperienceStepFormStateDelegate.current
    val text = remember { mutableStateOf(defaultValue ?: "") }

    LaunchedEffect(key1 = text.value) {
        formState.captureFormItem(this@Compose.id, SingleTextFormItem(label.text, required, text.value))
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = style.getHorizontalAlignment(),
    ) {
        label.Compose()

        // Several styling customization options for TextField noted here https://stackoverflow.com/a/68592613
        OutlinedTextField(
            value = text.value,
            onValueChange = {
                if (maxLength == null || it.length <= maxLength) {
                    text.value = it
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.applyStyle(
                style = textFieldStyle,
                context = LocalContext.current,
                isDark = isSystemInDarkTheme(),
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = numberOfLines,
            singleLine = numberOfLines == 1,
            placeholder = placeholder?.let {
                // future plan is to replace this with a text primitive directly
                {
                    Text(text = it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = mapKeyboardType(dataType)),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = Color.Black,
                focusedIndicatorColor = Color.LightGray,
                unfocusedIndicatorColor = Color.LightGray
            ),
        )
    }
}

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