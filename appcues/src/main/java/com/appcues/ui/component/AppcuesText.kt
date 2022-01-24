package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.domain.entity.ExperienceComponent.TextComponent
import com.appcues.domain.entity.styling.ComponentColor
import com.appcues.domain.entity.styling.ComponentStyle
import com.appcues.domain.entity.styling.ComponentTextAlignment
import com.appcues.ui.extensions.padding
import com.appcues.ui.theme.AppcuesPreview
import java.util.UUID

@Composable
internal fun TextComponent.Compose() {
    Text(
        text = text,
        modifier = Modifier.padding(style.padding()),
        color = getTextColor(isSystemInDarkTheme()),
        fontSize = textSize.sp,
        textAlign = textAlignment.toTextAlign(),
    )
}

private fun TextComponent.getTextColor(isDark: Boolean): Color {
    return if (isDark) {
        Color(textColor.dark)
    } else {
        Color(textColor.light)
    }
}

private fun ComponentTextAlignment.toTextAlign(): TextAlign {
    return when (this) {
        ComponentTextAlignment.LEADING -> TextAlign.Start
        ComponentTextAlignment.CENTER -> TextAlign.Center
        ComponentTextAlignment.TRAILING -> TextAlign.End
    }
}

private val textComponent = TextComponent(
    id = UUID.randomUUID(),
    text = "My Text Preview",
    textSize = 14,
    textColor = ComponentColor(light = 0xFF000000, dark = 0xFFFFFFFF),
    style = ComponentStyle(),
    textAlignment = ComponentTextAlignment.CENTER,
    fontName = "Comic-Sans",
    lineSpacing = 1,
)

@Composable
@Preview(name = "Text Color")
internal fun PreviewTextComponentColor() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val blackWhiteTextComponent = textComponent.copy(textColor = ComponentColor(light = 0xFF000000, dark = 0xFFFFFFFF))
        AppcuesPreview(isDark = true) {
            blackWhiteTextComponent.Compose()
        }
        AppcuesPreview(isDark = false) {
            blackWhiteTextComponent.Compose()
        }
        val redTextComponent = textComponent.copy(textColor = ComponentColor(light = 0xFFFF3030, dark = 0xFFFF3030))
        AppcuesPreview(isDark = true) {
            redTextComponent.Compose()
        }
        AppcuesPreview(isDark = false) {
            redTextComponent.Compose()
        }
    }
}
