package com.appcues.ui.component

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.resolveDefaults
import androidx.compose.ui.unit.dp
import com.appcues.domain.entity.ExperienceComponent.ButtonComponent
import com.appcues.ui.LocalAppcuesActions
import com.appcues.ui.extensions.Compose
import com.appcues.ui.extensions.applyStyle
import com.appcues.ui.extensions.componentStyle

@Composable
internal fun ButtonComponent.Compose() {
    val onClick = LocalAppcuesActions.current.onClick
    val textStyle = LocalTextStyle.current
    val layoutDirection = LocalLayoutDirection.current

    Button(
        shape = RoundedCornerShape(style.cornerRadius.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = { onClick?.invoke(id) },
    ) {
        Box(
            modifier = Modifier.componentStyle(style, isSystemInDarkTheme(), MaterialTheme.colors.primary),
            contentAlignment = Alignment.Center,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides resolveDefaults(textStyle, layoutDirection).applyStyle(
                    style = style,
                    context = LocalContext.current,
                    isDark = isSystemInDarkTheme(),
                )
            ) {
                content.Compose()
            }
        }
    }
}
