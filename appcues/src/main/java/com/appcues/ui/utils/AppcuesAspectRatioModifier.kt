package com.appcues.ui.utils

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentContentMode.FILL
import com.appcues.data.model.styling.ComponentContentMode.FIT
import com.appcues.ui.composables.StackScope
import com.appcues.ui.composables.StackScope.ColumnStackScope
import com.appcues.ui.composables.StackScope.RowStackScope
import kotlin.math.roundToInt

/**
 * This is an adaptation of the standard Compose AspectRatioModifier, but with logic updates to handle
 * the matchHeightConstraintsFirst dynamically, based on:
 *
 * (1) the contentMode (fit vs fill)
 * (2) the aspect ratio of the image and styling width and height
 * (3) the stackScope (row vs column) and the height of the row for side-by-side content
 *
 */

internal class AppcuesAspectRatioModifier(
    private val originalAspectRatio: Float,
    private val contentMode: ComponentContentMode,
    private val stackScope: StackScope,
    private val widthPixels: Float?,
    private val heightPixels: Float?,
) : LayoutModifier {

    init {
        require(originalAspectRatio > 0) { "aspectRatio $originalAspectRatio must be > 0" }
    }

    // takes the originalAspectRadio but it can later change based on width/height of the content
    private var aspectRatio: Float = originalAspectRatio

    // calculates parent view aspectRatio based on provided width and height
    private val parentAspectRatio: Float? = if (isPositiveFloat(widthPixels) && isPositiveFloat(heightPixels)) {
        widthPixels / heightPixels
    } else null

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val size = constraints.findSize()
        val wrappedConstraints = if (size != IntSize.Zero) {
            Constraints.fixed(size.width, size.height)
        } else {
            constraints
        }
        val placeable = measurable.measure(wrappedConstraints)
        return layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = if (height != Constraints.Infinity) {
        (height * aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicWidth(height)
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.minIntrinsicHeight(width)
    }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = if (width != Constraints.Infinity) {
        (width / aspectRatio).roundToInt()
    } else {
        measurable.maxIntrinsicHeight(width)
    }

    private fun Constraints.findSize(): IntSize {
        // finds the container aspect ratio based on provided width/height
        // if exists or defaults to limits by max container values
        val containerAspectRatio = (widthPixels ?: this.maxWidth.toFloat()) / (heightPixels ?: this.maxHeight.toFloat())

        // gets the size based on rules of matching first height or width. Also
        // takes into consideration if its a valid size when enforcing constraints or not
        val size = if (shouldMatchHeightFirst(containerAspectRatio)) {
            tryMatchHeightFirst(aspectRatio, widthPixels, true) ?: tryMatchHeightFirst(aspectRatio, widthPixels, false)
        } else {
            tryMatchWidthFirst(aspectRatio, widthPixels, true) ?: tryMatchWidthFirst(aspectRatio, widthPixels, false)
        }

        return size ?: IntSize.Zero
    }

    private fun Constraints.shouldMatchHeightFirst(
        containerAspectRatio: Float,
    ) = when (contentMode) {
        FILL -> shouldMatchHeightFirstFill(containerAspectRatio)
        FIT -> shouldMatchHeightFirstFit(containerAspectRatio)
    }

    private fun Constraints.shouldMatchHeightFirstFill(
        containerAspectRatio: Float
    ): Boolean {
        when (stackScope) {
            is ColumnStackScope -> {}
            is RowStackScope -> {
                val rowHeight = stackScope.greaterSize.value
                if (rowHeight != null) {
                    // we change the aspectRatio coming from the image when:
                    aspectRatio = when {
                        // both width and height are set in style
                        isPositiveFloat(widthPixels) && isPositiveFloat(heightPixels) -> widthPixels / heightPixels
                        // width is set in style and rowHeight has a valid value
                        isPositiveFloat(widthPixels) && rowHeight > 0 -> widthPixels.toFloat() / rowHeight
                        // rowHeight has a valid value
                        isPositiveFloat(rowHeight) -> this.maxWidth.toFloat() / rowHeight
                        else -> aspectRatio
                    }
                }
            }
        }

        return containerAspectRatio < aspectRatio
    }

    private fun shouldMatchHeightFirstFit(
        containerAspectRatio: Float,
    ) = when (stackScope) {
        is ColumnStackScope -> {
            containerAspectRatio > aspectRatio
        }
        is RowStackScope -> {

            if (parentAspectRatio != null) {
                aspectRatio = parentAspectRatio
            }

            when {
                isPositiveFloat(widthPixels) -> false
                isPositiveFloat(heightPixels) -> containerAspectRatio > aspectRatio
                else -> containerAspectRatio < aspectRatio
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? AppcuesAspectRatioModifier ?: return false
        return aspectRatio == otherModifier.aspectRatio &&
            contentMode == other.contentMode
    }

    override fun hashCode(): Int =
        aspectRatio.hashCode() * 31 + contentMode.hashCode()

    override fun toString(): String = "AspectRatioModifier(aspectRatio=$aspectRatio)"
}
