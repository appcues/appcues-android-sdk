package com.appcues.ui.modal

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.AppcuesTraitAnimatedVisibility
import com.appcues.ui.extensions.modalStyle

@Composable
internal fun FullScreenModal(style: ComponentStyle?, content: @Composable () -> Unit) {
    AppcuesTraitAnimatedVisibility {
        Surface(
            modifier = Modifier
                // will fill max size
                .fillMaxSize()
                // default modal style modifiers
                .modalStyle(
                    style = style,
                    isDark = isSystemInDarkTheme()
                ),
            content = content,
        )
    }
}
