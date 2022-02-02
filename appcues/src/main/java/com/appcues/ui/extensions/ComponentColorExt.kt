package com.appcues.ui.extensions

import androidx.compose.ui.graphics.Color
import com.appcues.domain.entity.styling.ComponentColor

internal fun ComponentColor.getColor(isDark: Boolean) = Color(if (isDark) dark else light)

internal fun ComponentColor?.getColor(isDark: Boolean): Color? {
    if (this == null) return null

    return Color(if (isDark) dark else light)
}
