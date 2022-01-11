package com.appcues.ui.theme

import android.content.Context
import androidx.compose.material.Colors
import androidx.compose.ui.graphics.Color
import com.appcues.R.attr
import com.google.android.material.color.MaterialColors

internal fun colors(context: Context, baseColors: Colors): Colors {
    return baseColors.copy(
        primary = Color(MaterialColors.getColor(context, attr.colorPrimary, baseColors.primary.toString()))
    )
}
