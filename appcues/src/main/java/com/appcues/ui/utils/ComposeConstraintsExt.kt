package com.appcues.ui.utils

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.isSatisfiedBy
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.math.roundToInt

internal fun Constraints.tryMatchHeightFirst(aspectRatio: Float, widthPixels: Float?, enforceConstraints: Boolean): IntSize? {
    // tryMaxHeight, if null tryMaxWidth
    val maxSize = tryMaxHeight(aspectRatio, widthPixels, enforceConstraints) ?: tryMaxWidth(aspectRatio, widthPixels, enforceConstraints)
    // tryMinHeight, if null tryMinWidth
    val minSize = tryMinHeight(aspectRatio, enforceConstraints) ?: tryMinWidth(aspectRatio, enforceConstraints)
    // return maxSize, if null minSize else null
    return (maxSize ?: minSize)
}

internal fun Constraints.tryMatchWidthFirst(aspectRatio: Float, widthPixels: Float?, enforceConstraints: Boolean): IntSize? {
    // tryMaxWidth, if null tryMaxHeight
    val maxSize = tryMaxWidth(aspectRatio, widthPixels, enforceConstraints) ?: tryMaxHeight(aspectRatio, widthPixels, enforceConstraints)
    // tryMinWidth, if null tryMinHeight
    val minSize = tryMinWidth(aspectRatio, enforceConstraints) ?: tryMinHeight(aspectRatio, enforceConstraints)
    // return maxSize, if null minSize else null
    return (maxSize ?: minSize)
}

@OptIn(ExperimentalContracts::class)
internal fun isPositiveFloat(value: Float?): Boolean {
    contract {
        returns(true) implies (value != null)
    }

    return value != null && value > 0.0
}

private fun Constraints.tryMaxWidth(aspectRatio: Float, widthPixels: Float?, enforceConstraints: Boolean): IntSize? {
    val maxWidth = if (isPositiveFloat(widthPixels)) widthPixels else this.maxWidth
    if (maxWidth != Constraints.Infinity) {
        val height = (maxWidth.toFloat() / aspectRatio).roundToInt()
        if (height > 0) {
            val size = IntSize(maxWidth.toInt(), height)
            if (widthPixels != null || !enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
    }
    return null
}

private fun Constraints.tryMaxHeight(aspectRatio: Float, widthPixels: Float?, enforceConstraints: Boolean): IntSize? {
    val maxHeight = this.maxHeight
    val maxWidth = if (isPositiveFloat(widthPixels)) widthPixels else this.maxWidth.toFloat()
    val height = (maxWidth / aspectRatio).roundToInt()
    if (maxHeight != Constraints.Infinity) {
        val width = (maxHeight * aspectRatio).roundToInt()
        if (width > 0) {
            val size = IntSize(width, maxHeight)
            if (!enforceConstraints || isSatisfiedBy(size)) {
                return size
            }
        }
    } else if (height > 0) {
        return IntSize(maxWidth.toInt(), height)
    }
    return null
}

private fun Constraints.tryMinWidth(aspectRatio: Float, enforceConstraints: Boolean = true): IntSize? {
    val minWidth = this.minWidth
    val height = (minWidth / aspectRatio).roundToInt()
    if (height > 0) {
        val size = IntSize(minWidth, height)
        if (!enforceConstraints || isSatisfiedBy(size)) {
            return size
        }
    }
    return null
}

private fun Constraints.tryMinHeight(aspectRatio: Float, enforceConstraints: Boolean = true): IntSize? {
    val minHeight = this.minHeight
    val width = (minHeight * aspectRatio).roundToInt()
    if (width > 0) {
        val size = IntSize(width, minHeight)
        if (!enforceConstraints || isSatisfiedBy(size)) {
            return size
        }
    }
    return null
}
