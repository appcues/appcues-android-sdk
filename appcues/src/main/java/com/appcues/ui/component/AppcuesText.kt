package com.appcues.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.styling.ComponentTextAlignment
import com.appcues.ui.extensions.padding

@Composable
internal fun TextComponent.Compose() {
    Text(
        text = text,
        modifier = Modifier.padding(style.padding()),
        color = Color(textColor.light),
        fontSize = textSize.sp,
        textAlign = textAlignment.toTextAlign(),
    )
}

private fun ComponentTextAlignment.toTextAlign(): TextAlign {
    return when (this) {
        ComponentTextAlignment.START -> TextAlign.Start
        ComponentTextAlignment.CENTER -> TextAlign.Center
        ComponentTextAlignment.END -> TextAlign.End
        ComponentTextAlignment.JUSTIFY -> TextAlign.Justify
    }
}
