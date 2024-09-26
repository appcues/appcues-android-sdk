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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset

@Stable
internal fun Modifier.margin(paddingValues: PaddingValues) =
    this.then(
        MarginValuesModifier(
            paddingValues = paddingValues,
            inspectorInfo = debugInspectorInfo {
                name = "margin"
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

// This is a copy/paste of the standard PaddingValuesImpl from compose foundation, but without
// the restriction against negative values, as we allow negative margins and handle that above
// in our MarginValuesModifier. Prior to Compose 1.7 we still used the standard PaddingValuesImpl,
// but in Compose 1.7 they added checks that throw exceptions if negative values are used, so
// we need our own.
//
// This implementation still implements the same PaddingValues interface, so there is minimal impact to
// existing call sites.
//
// the change to check negative values was in this update:
// https://android-review.googlesource.com/c/platform/frameworks/support/+/3021136/3/compose/foundation/foundation-layout/src/commonMain/kotlin/androidx/compose/foundation/layout/Padding.kt
@Stable
@Suppress("FunctionNaming") // matching Compose naming for PaddingValues here
internal fun MarginValues(all: Dp): PaddingValues = MarginValues(all, all, all, all)

internal class MarginValues(
    @Stable
    val start: Dp = 0.dp,
    @Stable
    val top: Dp = 0.dp,
    @Stable
    val end: Dp = 0.dp,
    @Stable
    val bottom: Dp = 0.dp
) : PaddingValues {

    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding() = top

    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding() = bottom

    override fun equals(other: Any?): Boolean {
        if (other !is MarginValues) return false
        return start == other.start &&
            top == other.top &&
            end == other.end &&
            bottom == other.bottom
    }

    override fun hashCode() =
        ((start.hashCode() * 31 + top.hashCode()) * 31 + end.hashCode()) * 31 + bottom.hashCode()

    override fun toString() = "MarginValues(start=$start, top=$top, end=$end, bottom=$bottom)"
}
