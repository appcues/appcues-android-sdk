package com.appcues.ui.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.styling.ComponentStyle

internal fun Modifier.componentStyle(
    component: ExperiencePrimitive,
    gestureProperties: PrimitiveGestureProperties,
    isDark: Boolean,
    defaultBackgroundColor: Color? = null,
    noSizeFillMax: Boolean = false,
) = this.then(
    with(component) {
        Modifier
            .padding(style.getMargins())
            .componentShadow(style, isDark)
            .componentSize(style, noSizeFillMax)
            .componentActions(actions, gestureProperties)
            .componentCorner(style)
            .componentBorder(style, isDark)
            .componentBackground(style, isDark, defaultBackgroundColor)
            .padding(style.getPaddings())
    }
)

internal fun Modifier.componentBackground(
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

internal fun Modifier.componentBorder(
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

internal fun Modifier.componentSize(style: ComponentStyle, noSizeFillMax: Boolean = false) = this.then(
    when {
        style.width != null && style.height != null ->
            Modifier.size(style.width.dp, style.height.dp)
        style.width != null ->
            Modifier.width(style.width.dp)
        style.height != null ->
            Modifier.height(style.height.dp)
        noSizeFillMax ->
            Modifier.fillMaxSize()
        else -> Modifier
    }
)

internal fun Modifier.componentCorner(style: ComponentStyle) = this.then(
    when {
        style.cornerRadius != 0 -> Modifier.clip(RoundedCornerShape(style.cornerRadius.dp))
        else -> Modifier
    }
)

private fun Modifier.componentShadow(style: ComponentStyle, isDark: Boolean): Modifier {
    return this.then(
        when {
            style.shadow != null -> Modifier.coloredShadow(
                color = style.shadow.color.getColor(isDark),
                radius = style.shadow.radius.dp,
                offsetX = style.shadow.x.dp,
                offsetY = style.shadow.y.dp,
            )
            else -> Modifier
        }
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
    val interactionSource: MutableInteractionSource,
    val indication: Indication?,
    val enabled: Boolean = true,
    val role: Role = Role.Button,
)

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.componentActions(
    actions: List<Action>,
    gestureProperties: PrimitiveGestureProperties
) = this.then(
    // check to see if the list is not null first, to ensure that we have at least one action before setting
    // the combinedClickable Modifier
    if (actions.isNotEmpty()) Modifier.combinedClickable(
        interactionSource = gestureProperties.interactionSource,
        indication = gestureProperties.indication,
        enabled = gestureProperties.enabled,
        role = gestureProperties.role,
        onLongClick = actions.toLongPressMotionOrNull(gestureProperties.onAction),
        onClick = actions.toTapMotionOrEmpty(gestureProperties.onAction),
    )
    else Modifier
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
