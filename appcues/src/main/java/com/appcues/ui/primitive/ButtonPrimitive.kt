package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.appcues.data.model.ExperiencePrimitive.ButtonPrimitive
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getTextStyle

@Composable
internal fun ButtonPrimitive.Compose(modifier: Modifier) {
    Surface(
        modifier = modifier,
        color = Color.Transparent,
    ) {
        ProvideTextStyle(style.getTextStyle()) {
            Box(
                contentAlignment = content.style.getBoxAlignment(),
                modifier = Modifier.styleButtonContentWidth(style),
            ) {
                content.Compose()
            }
        }
    }
}

private fun Modifier.styleButtonContentWidth(buttonStyle: ComponentStyle) =
    if (buttonStyle.width != null) {
        this.fillMaxWidth()
    } else {
        Modifier
    }
