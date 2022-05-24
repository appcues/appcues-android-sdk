package com.appcues.ui.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.StepDecoratingPadding
import java.util.UUID

internal fun Modifier.primitiveStyle(
    component: ExperiencePrimitive,
    gestureProperties: PrimitiveGestureProperties,
    isDark: Boolean,
    defaultBackgroundColor: Color? = null,
    noSizeFillMax: Boolean = false,
) = this.then(
    with(component) {
        Modifier
            .padding(style.getMargins())
            .styleShadow(style, isDark)
            .styleSize(style, noSizeFillMax)
            .actions(id, gestureProperties)
            .styleCorner(style)
            .styleBorder(style, isDark)
            .styleBackground(style, isDark, defaultBackgroundColor)
            .padding(style.getPaddings())
    }
)

internal fun Modifier.modalStyle(
    style: ComponentStyle?,
    isDark: Boolean,
    modifier: Modifier = Modifier,
) = this.then(
    if (style != null) Modifier
        .padding(style.getMargins())
        .then(modifier)
        .styleBorder(style, isDark)
        .styleBackground(style, isDark)
    else Modifier
)

private fun Modifier.styleBackground(
    style: ComponentStyle,
    isDark: Boolean,
    defaultColor: Color? = null
) = this.then(
    when {
        style.backgroundGradient != null -> Modifier.background(
            Brush.horizontalGradient(style.backgroundGradient.map { Color(if (isDark) it.dark else it.light) })
        )
        style.backgroundColor != null -> Modifier.background(style.backgroundColor.getColor(isDark))
        defaultColor != null -> Modifier.background(defaultColor)
        else -> Modifier
    }
)

private fun Modifier.styleBorder(
    style: ComponentStyle,
    isDark: Boolean
) = this.then(
    if (style.borderWidth != null && style.borderWidth != 0 && style.borderColor != null) {
        Modifier
            .border(style.borderWidth.dp, style.borderColor.getColor(isDark), RoundedCornerShape(style.cornerRadius.dp))
    } else {
        Modifier
    }
)

internal fun Modifier.styleSize(style: ComponentStyle, noSizeFillMax: Boolean = false) = this.then(
    when {
        // when style contains both width and height
        style.width != null && style.height != null -> when {
            // we fill max if both properties are -1
            style.width == -1 && style.height == -1 -> Modifier.fillMaxSize()
            // or we set height and fill width in case only width is -1
            style.width == -1 ->
                Modifier
                    .height(style.height.dp)
                    .fillMaxWidth()
            // or we set width and fill height in case only height is -1
            style.height == -1 ->
                Modifier
                    .width(style.width.dp)
                    .fillMaxHeight()
            // else we set size with width and height
            else -> Modifier.size(style.width.dp, style.height.dp)
        }
        // if only width is not null, we fill max in case its -1 else we set the width
        style.width != null -> if (style.width == -1) Modifier.fillMaxWidth() else Modifier.width(style.width.dp)
        // if only height is not null, we fill max in case its -1 else we set the height
        style.height != null -> if (style.height == -1) Modifier.fillMaxHeight() else Modifier.height(style.height.dp)
        // at the end we fill max size in case there is no width/height but the primitive (like image) is fill max by default
        noSizeFillMax -> Modifier.fillMaxSize()
        else -> Modifier
    }
)

private fun Modifier.styleCorner(style: ComponentStyle) = this.then(
    when {
        style.cornerRadius != 0 -> Modifier.clip(RoundedCornerShape(style.cornerRadius.dp))
        else -> Modifier
    }
)

internal fun Modifier.styleShadow(style: ComponentStyle?, isDark: Boolean): Modifier {
    return this.then(
        if (style?.shadow != null) Modifier.coloredShadow(
            color = style.shadow.color.getColor(isDark),
            radius = style.shadow.radius.dp,
            offsetX = style.shadow.x.dp,
            offsetY = style.shadow.y.dp,
        )
        else Modifier
    )
}

private fun Modifier.coloredShadow(
    color: Color,
    radius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) = drawBehind {

    val shadowColor = color.toArgb()
    val transparent = color.copy(alpha = 0.2f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent

        frameworkPaint.setShadowLayer(
            4.dp.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )

        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            radius.toPx(),
            radius.toPx(),
            paint
        )
    }
}

internal data class PrimitiveGestureProperties(
    val onAction: (ExperienceAction) -> Unit,
    val actions: Map<UUID, List<Action>>,
    val interactionSource: MutableInteractionSource,
    val indication: Indication?,
    val enabled: Boolean = true,
    val role: Role = Role.Button,
)

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.actions(
    id: UUID,
    gestureProperties: PrimitiveGestureProperties
) = this.then(
    // check to see if the list is not null first, to ensure that we have at least one action before setting
    // the combinedClickable Modifier
    with(gestureProperties.actions[id]) {
        if (isNullOrEmpty()) Modifier else
            Modifier.combinedClickable(
                interactionSource = gestureProperties.interactionSource,
                indication = gestureProperties.indication,
                enabled = gestureProperties.enabled,
                role = gestureProperties.role,
                onLongClick = toLongPressMotionOrNull(gestureProperties.onAction),
                onClick = toTapMotionOrEmpty(gestureProperties.onAction),
            )
    }
)

private fun List<Action>.toTapMotionOrEmpty(onAction: (ExperienceAction) -> Unit): (() -> Unit) {
    // filter only TAP motions
    return filter { it.on == Action.Motion.TAP }
        // take if there is any
        .takeIf { it.isNotEmpty() }
        // map to ExperienceAction
        ?.map { it.experienceAction }
        // return click block that executes onAction for every ExperienceAction
        // else returns empty click block (notnull property for combinedClickable)
        ?.run { { forEach { onAction(it) } } } ?: { }
}

private fun List<Action>.toLongPressMotionOrNull(block: (ExperienceAction) -> Unit): (() -> Unit)? {
    // filter only LONG_PRESS motions
    return filter { it.on == Action.Motion.LONG_PRESS }
        // take if there is any
        .takeIf { it.isNotEmpty() }
        // map to ExperienceAction
        ?.map { it.experienceAction }
        // return click block that executes onAction for every ExperienceAction
        // else returns null
        ?.run { { forEach { block(it) } } }
}

internal fun Modifier.imageAspectRatio(intrinsicSize: ComponentSize?) = this.then(
    // apply aspectRatio only when intrinsicSize is not null or any values is bigger than 0
    if (intrinsicSize != null && (intrinsicSize.width > 0 && intrinsicSize.height > 0)) {
        Modifier.aspectRatio(ratio = intrinsicSize.width.toFloat() / intrinsicSize.height.toFloat(), matchHeightConstraintsFirst = true)
    } else Modifier
)

/**
 * used to properly align step decorating trait content
 *
 * the main reason why we have this function is so that we do some internal calculations
 * to decide the amount of padding the main content will apply based on all step decorating traits
 * applied for that step
 */
fun Modifier.alignStepOverlay(
    boxScope: BoxScope,
    alignment: Alignment,
    stepDecoratingPadding: StepDecoratingPadding
): Modifier {
    return with(boxScope) {
        then(
            Modifier
                .align(alignment)
                .onGloballyPositioned {
                    if (alignment is BiasAlignment) {
                        with(alignment) {
                            when {
                                horizontalBias == -1f && verticalBias == 0f -> stepDecoratingPadding.setStartPadding(it.size.width)
                                horizontalBias == 1f && verticalBias == 0f -> stepDecoratingPadding.setEndPadding(it.size.width)
                                verticalBias == -1f -> stepDecoratingPadding.setTopPadding(it.size.height)
                                verticalBias == 1f -> stepDecoratingPadding.setBottomPadding(it.size.height)
                            }
                        }
                    }
                }
        )
    }
}
