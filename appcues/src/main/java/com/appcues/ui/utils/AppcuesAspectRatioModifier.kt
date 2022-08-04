package com.appcues.ui.utils

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isSatisfiedBy
import com.appcues.data.model.styling.ComponentContentMode
import kotlin.math.roundToInt

/**
 * This is an adaptation of the standard Compose AspectRatioModifier, but with logic updates to handle
 * the matchHeightConstraintsFirst dynamically, based on (1) the contentMode (fit vs fill) and (2) the aspect
 * ratio of the view being fit within.
*/

internal class AppcuesAspectRatioModifier(
    private val aspectRatio: Float,
    private val contentMode: ComponentContentMode,
) : LayoutModifier {
    init {
        require(aspectRatio > 0) { "aspectRatio $aspectRatio must be > 0" }
    }

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

    // Suppressing these, as this method is taken from the Compose source and adapted slightly to add the
    // logic based on contentMode.  Attempting to rewrite this differently would be a more confusing exercise at this point.
    @Suppress("ComplexMethod", "ReturnCount")
    private fun Constraints.findSize(): IntSize {

        val containerAspectRatio = this.maxWidth.toDouble() / this.maxHeight.toDouble()
        val matchHeightConstraintsFirst =
            (contentMode == ComponentContentMode.FIT && containerAspectRatio > aspectRatio) ||
                (contentMode == ComponentContentMode.FILL && containerAspectRatio < aspectRatio)

        if (!matchHeightConstraintsFirst) {
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
            tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
        } else {
            tryMaxHeight().also { if (it != IntSize.Zero) return it }
            tryMaxWidth().also { if (it != IntSize.Zero) return it }
            tryMinHeight().also { if (it != IntSize.Zero) return it }
            tryMinWidth().also { if (it != IntSize.Zero) return it }
            tryMaxHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMaxWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinHeight(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
            tryMinWidth(enforceConstraints = false).also { if (it != IntSize.Zero) return it }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxWidth(enforceConstraints: Boolean = true): IntSize {
        val maxWidth = this.maxWidth
        if (maxWidth != Constraints.Infinity) {
            val height = (maxWidth / aspectRatio).roundToInt()
            if (height > 0) {
                val size = IntSize(maxWidth, height)
                if (!enforceConstraints || isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMaxHeight(enforceConstraints: Boolean = true): IntSize {
        val maxHeight = this.maxHeight
        if (maxHeight != Constraints.Infinity) {
            val width = (maxHeight * aspectRatio).roundToInt()
            if (width > 0) {
                val size = IntSize(width, maxHeight)
                if (!enforceConstraints || isSatisfiedBy(size)) {
                    return size
                }
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinWidth(enforceConstraints: Boolean = true): IntSize {
        val minWidth = this.minWidth
        val height = (minWidth / aspectRatio).roundToInt()
        if (height > 0) {
            val size = IntSize(minWidth, height)
            if (!enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
    }

    private fun Constraints.tryMinHeight(enforceConstraints: Boolean = true): IntSize {
        val minHeight = this.minHeight
        val width = (minHeight * aspectRatio).roundToInt()
        if (width > 0) {
            val size = IntSize(width, minHeight)
            if (!enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
        return IntSize.Zero
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
