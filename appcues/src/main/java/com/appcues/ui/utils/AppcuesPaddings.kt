package com.appcues.ui.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.InspectorValueInfo
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.offset

@Stable
internal fun Modifier.appcuesPaddings(paddingValues: PaddingValues) =
    this.then(
        AppcuesPaddingValueModifier(
            paddingValues = paddingValues,
            inspectorInfo = debugInspectorInfo {
                name = "appcuesPaddings"
                properties["paddingValues"] = paddingValues
            }
        )
    )

private class AppcuesPaddingValueModifier(
    val paddingValues: PaddingValues,
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        // normalize paddings to remove negative values
        val leftPadding = paddingValues.calculateLeftPadding(layoutDirection).roundToPx().coerceAtLeast(0)
        val rightPadding = paddingValues.calculateRightPadding(layoutDirection).roundToPx().coerceAtLeast(0)
        val topPadding = paddingValues.calculateTopPadding().roundToPx().coerceAtLeast(0)
        val bottomPadding = paddingValues.calculateBottomPadding().roundToPx().coerceAtLeast(0)
        val horizontal = leftPadding + rightPadding
        val vertical = topPadding + bottomPadding

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        val width = constraints.constrainWidth(placeable.width + horizontal)
        val height = constraints.constrainHeight(placeable.height + vertical)

        return layout(width, height) {
            placeable.place(leftPadding, topPadding)
        }
    }

    override fun hashCode() = paddingValues.hashCode()

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? AppcuesPaddingValueModifier ?: return false
        return paddingValues == otherModifier.paddingValues
    }
}
