package com.appcues.ui.extensions

import com.appcues.data.model.ExperiencePrimitive.TextPrimitive

// helper to optionally adapt this Text to use the foreground color of a different Text, as an error state
// used in form validation
internal fun TextPrimitive.checkErrorStyle(showError: Boolean, errorLabel: TextPrimitive?): TextPrimitive {
    val errorTint = if (showError) errorLabel?.style?.foregroundColor else null
    return if (errorTint != null) {
        // use a copy of the label with a modified foreground color
        copy(style = style.copy(foregroundColor = errorTint))
    } else {
        // use unchanged label
        this
    }
}
