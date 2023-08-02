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
import kotlin.math.roundToInt

internal class AspectRatioContentModeModifier(
    private val contentMode: ComponentContentMode,
    private val ratioWidth: Float,
    private val ratioHeight: Float,
    private val fixedWidth: Int?,
    private val fixedHeight: Int?,
) : LayoutModifier {

    init {
        require(ratioWidth > 0) { "ratioWidth $ratioWidth must be > 0" }
        require(ratioHeight > 0) { "ratioHeight $ratioHeight must be > 0" }
    }

    private val ratio = ratioWidth / ratioHeight

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
    ) = fixedWidth
        ?: if (height != Constraints.Infinity) {
            if (fixedHeight != null) {
                (fixedHeight * ratio).roundToInt()
            } else {
                (height * ratio).roundToInt()
            }
        } else {
            measurable.minIntrinsicWidth(height)
        }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurable: IntrinsicMeasurable,
        height: Int
    ) = fixedWidth
        ?: if (height != Constraints.Infinity) {
            (height * ratio).roundToInt()
        } else {
            measurable.maxIntrinsicWidth(height)
        }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = fixedHeight
        ?: if (width != Constraints.Infinity) {
            if (fixedWidth != null) {
                (fixedWidth / ratio).roundToInt()
            } else {
                (width / ratio).roundToInt()
            }
        } else {
            measurable.minIntrinsicHeight(width)
        }

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurable: IntrinsicMeasurable,
        width: Int
    ) = fixedHeight
        ?: if (width != Constraints.Infinity) {
            (width / ratio).roundToInt()
        } else {
            measurable.maxIntrinsicHeight(width)
        }

    private fun Constraints.findSize(): IntSize {
        // If a fixed size is being applied don't bother at all with aspect ratio calculation,
        // and maintain the fixed size.
        if (fixedWidth != null && fixedHeight != null) {
            return IntSize(fixedWidth, fixedHeight)
        }

        val unboundHeight = maxHeight == Constraints.Infinity

        val widthToUse = fixedWidth ?: maxWidth
        val maxHeightToUse = if (unboundHeight) (widthToUse / ratio).toInt() else maxHeight
        val containerAspect = widthToUse.toDouble() / maxHeightToUse.toDouble()

        val matchWidth = when (contentMode) {
            ComponentContentMode.FIT -> containerAspect < ratio
            ComponentContentMode.FILL -> containerAspect > ratio
        }

        return if (matchWidth) {
            IntSize(widthToUse, (widthToUse * ratioHeight / ratioWidth).roundToInt())
        } else {
            IntSize((maxHeightToUse * ratioWidth / ratioHeight).roundToInt(), maxHeightToUse)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        val otherModifier = other as? AspectRatioContentModeModifier ?: return false
        return ratio == otherModifier.ratio &&
            contentMode == other.contentMode &&
            fixedWidth == other.fixedHeight &&
            fixedHeight == other.fixedHeight
    }

    override fun hashCode(): Int =
        ratio.hashCode() * 31 + contentMode.hashCode() + fixedWidth.hashCode() + fixedHeight.hashCode()

    override fun toString(): String = "AspectRatioContentModeModifier(" +
        "contentMode=$contentMode" +
        "ratioWidth=$ratioWidth, " +
        "ratioHeight=$ratioHeight, " +
        "fixedWidth=$fixedWidth," +
        "fixedHeight=$fixedHeight" +
        ")"
}
