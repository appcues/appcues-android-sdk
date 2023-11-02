package com.appcues.util

import android.content.Context
import android.graphics.Rect
import android.util.Size
import android.view.View
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.core.graphics.Insets
import com.appcues.util.ContextDensity.ContextDensityImpl
import kotlin.math.roundToInt

/**
 * Very loose interface that converts values without changing its type.
 *
 * An [Int] that contains a Pixel value can be converted to [Int] that contains Dp value
 */
internal interface ContextDensity {

    class ContextDensityImpl(private val context: Context) : ContextDensity {

        override val density: Float
            get() = context.resources.displayMetrics.density
    }

    /**
     * The logical density of the display. This is a scaling factor for the [Dp] unit.
     */
    @Stable
    val density: Float

    /**
     * Convert [Dp] to pixels. Pixels are used to paint to Canvas.
     */
    @Stable
    fun Int.toPx(): Int = (this * density).roundToInt()

    /**
     * Convert an [Int] pixel value to [Int].
     */
    fun Int.toDp(): Int = (this / density).roundToInt()

    /**
     * Convert an [Insets] pixel values to [Insets] as Dp
     */
    fun Insets.toDp(): Insets = Insets.of(left.toDp(), top.toDp(), right.toDp(), bottom.toDp())

    /**
     * Convert an [Rect] pixel values to [Rect] as Dp
     */
    fun Rect.toDp(): Rect = Rect(left.toDp(), top.toDp(), right.toDp(), bottom.toDp())

    /**
     * Convert an [Size] pixel values to [Size] as Dp
     */
    fun Size.toDp(): Size = Size(width.toDp(), height.toDp())
}

internal fun <R> View.withDensity(densityBlock: ContextDensity.() -> R): R {
    return with(ContextDensityImpl(context)) { densityBlock() }
}

internal fun <R> Context.withDensity(densityBlock: ContextDensity.() -> R): R {
    return with(ContextDensityImpl(this)) { densityBlock() }
}
