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
internal fun Modifier.appcuesMargins(paddingValues: PaddingValues) =
    this.then(
        MarginValuesModifier(
            paddingValues = paddingValues,
            inspectorInfo = debugInspectorInfo {
                name = "appcuesMargins"
                properties["marginValues"] = paddingValues
            }
        )
    )

private class MarginValuesModifier(
    val paddingValues: PaddingValues,
    inspectorInfo: InspectorInfo.() -> Unit
) : LayoutModifier, InspectorValueInfo(inspectorInfo) {

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints
    ): MeasureResult {
        val horizontal = paddingValues.calculateLeftPadding(layoutDirection).roundToPx() +
            paddingValues.calculateRightPadding(layoutDirection).roundToPx()
        val vertical = paddingValues.calculateTopPadding().roundToPx() +
            paddingValues.calculateBottomPadding().roundToPx()

        val placeable = measurable.measure(constraints.offset(-horizontal, -vertical))

        val width = constraints.constrainWidth(placeable.width + horizontal)
        val height = constraints.constrainHeight(placeable.height + vertical)
        return layout(width, height) {
            placeable.place(
                paddingValues.calculateLeftPadding(layoutDirection).roundToPx(),
                paddingValues.calculateTopPadding().roundToPx()
            )
        }
    }

    override fun hashCode() = paddingValues.hashCode()

    override fun equals(other: Any?): Boolean {
        val otherModifier = other as? MarginValuesModifier ?: return false
        return paddingValues == otherModifier.paddingValues
    }
}
