package com.appcues.ui.utils

import androidx.compose.foundation.layout.Arrangement.HorizontalOrVertical
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal object AppcuesArrangement {

    internal fun spacedCenter(space: Int = 0) = object : HorizontalOrVertical {
        override val spacing = space.dp

        override fun toString() = "AppcuesArrangement#spacedCenter"

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray
        ) = placeSpacedCenter(
            totalSize = totalSize,
            sizes = sizes,
            outPosition = outPositions,
            spacingPx = space.dp.roundToPx(),
            reverseInput = layoutDirection == LayoutDirection.Rtl
        )

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            outPositions: IntArray
        ) = arrange(totalSize, sizes, LayoutDirection.Ltr, outPositions)

        private fun placeSpacedCenter(
            totalSize: Int,
            sizes: IntArray,
            outPosition: IntArray,
            spacingPx: Int,
            reverseInput: Boolean,
        ) {
            // If there are not children, then do nothing
            if (sizes.isEmpty()) return
            // get combined value for all sizes for all children
            val combinedSizes = sizes.fold(0) { a, b -> a + b }
            // calculated the total consumed value considering all spacing
            val consumedSize = combinedSizes + (spacingPx * sizes.size) - spacingPx
            // set start position for the first child
            var current = (totalSize - consumedSize).toFloat() / 2
            sizes.forEachIndexed(reverseInput) { index, it ->
                // set position for item [index]
                outPosition[index] = current.roundToInt()
                // move current for the next item considering spacing
                current += it.toFloat() + spacingPx
            }
        }
    }

    internal fun spacedEvenly(space: Int = 0) = object : HorizontalOrVertical {
        override val spacing = space.dp

        override fun toString() = "AppcuesArrangement#spacedEvenly"

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            layoutDirection: LayoutDirection,
            outPositions: IntArray
        ) = placeSpacedEvenly(
            totalSize = totalSize,
            sizes = sizes,
            outPosition = outPositions,
            spacingPx = space.dp.roundToPx(),
            reverseInput = layoutDirection == LayoutDirection.Rtl
        )

        override fun Density.arrange(
            totalSize: Int,
            sizes: IntArray,
            outPositions: IntArray
        ) = arrange(totalSize, sizes, LayoutDirection.Ltr, outPositions)

        private fun placeSpacedEvenly(
            totalSize: Int,
            sizes: IntArray,
            outPosition: IntArray,
            spacingPx: Int,
            reverseInput: Boolean
        ) {
            // If there are not children, then do nothing
            if (sizes.isEmpty()) return
            // get combined value for all sizes for all children
            val combinedSizes = sizes.fold(0) { a, b -> a + b }
            // calculated the total consumed value considering all spacing
            val consumedSize = combinedSizes + (spacingPx * sizes.size) - spacingPx
            // find what is the gapSize to place all elements evenly
            val gapSize = (totalSize - consumedSize).toFloat() / (sizes.size + 1)
            // set start position for the first child
            var current = gapSize
            sizes.forEachIndexed(reverseInput) { index, it ->
                // set position for item [index]
                outPosition[index] = current.roundToInt()
                // move current for the next item considering spacing
                current += it.toFloat() + gapSize + spacingPx
            }
        }
    }

    private inline fun IntArray.forEachIndexed(reversed: Boolean, action: (Int, Int) -> Unit) {
        if (!reversed) {
            forEachIndexed(action)
        } else {
            for (i in (size - 1) downTo 0) {
                action(i, get(i))
            }
        }
    }
}
