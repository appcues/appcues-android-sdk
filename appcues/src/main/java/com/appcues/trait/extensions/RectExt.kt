package com.appcues.trait.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

internal fun Rect.getRectEncompassesRadius(blurRadius: Float): Float {
    return max((sqrt(width.pow(2) + height.pow(2)) / 2) + blurRadius, 0f)
}

internal fun Rect?.inflateOrEmpty(spreadRadius: Double): Rect {
    return this?.inflate(spreadRadius.toFloat()) ?: Rect(Offset(0f, 0f), Size(0f, 0f))
}
