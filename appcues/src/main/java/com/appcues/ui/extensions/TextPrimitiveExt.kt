package com.appcues.ui.extensions

import com.appcues.data.model.ExperiencePrimitive.TextPrimitive

// helper to optionally adapt this Text to use the foreground color of a different Text, as an error state
// used in form validation
internal fun TextPrimitive.checkErrorStyle(showError: Boolean, errorLabel: TextPrimitive?): TextPrimitive {
    val errorTint = if (showError) errorLabel?.style?.foregroundColor else null
    return if (errorTint != null) {
        // need to push this foreground color down into each text span (or single in default case)
        // for styling to properly apply when rendered into UI
        copy(
            spans = spans.map {
                it.copy(style = it.style.copy(foregroundColor = errorTint))
            }
        )
    } else {
        // use unchanged label
        this
    }
}
