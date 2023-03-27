package com.appcues.ui.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.Action
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.ImagePrimitive
import com.appcues.data.model.styling.ComponentContentMode
import com.appcues.data.model.styling.ComponentSize
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.StackScope
import com.appcues.ui.utils.AppcuesAspectRatioModifier
import com.appcues.ui.utils.margin
import com.appcues.util.eq
import com.appcues.util.ne
import java.util.UUID

/**
 * Used by ExperiencePrimitive Compose
 */
internal fun Modifier.outerPrimitiveStyle(
    component: ExperiencePrimitive,
    gestureProperties: PrimitiveGestureProperties,
    isDark: Boolean,
    matchParentBox: BoxScope? = null,
) = this.then(
    with(component) {
        Modifier
            .margin(style.getMargins())
            .styleShadow(style, isDark)
            .styleSize(style, matchParentBox)
            .actions(id, gestureProperties, component.textDescription)
            .styleCorner(style)
            .styleBackground(style, isDark)
            .styleBorder(style, isDark)
    }
)

/**
 * Should be used by any children of ExperiencePrimitive Compose
 *
 * eg. ButtonPrimitive, ImagePrimitive
 */
internal fun Modifier.innerPrimitiveStyle(component: ExperiencePrimitive) = this.then(
    when (component) {
        // image primitive needs to be clipped to bound to avoid
        // content drawing overflow (side-by-side primitives)
        is ImagePrimitive -> Modifier.clipToBounds()
        else -> Modifier
    }.padding(component.style.getPaddings())
)

internal fun Modifier.modalStyle(
    style: ComponentStyle?,
    isDark: Boolean,
    modalModifier: (ComponentStyle) -> Modifier,
) = this.then(
    if (style != null) Modifier
        .padding(style.getMargins())
        .then(modalModifier(style))
        .styleBackground(style, isDark)
        .styleBorder(style, isDark)
    else Modifier
)

internal fun Modifier.styleBackground(
    style: ComponentStyle?,
    isDark: Boolean,
) = this.then(
    if (style != null) {
        Modifier
            .styleBackgroundColor(style, isDark)
            .styleBackgroundGradient(style, isDark)
    } else Modifier
)

private fun Modifier.styleBackgroundColor(style: ComponentStyle, isDark: Boolean) = this.then(
    if (style.backgroundColor != null)
        Modifier.background(style.backgroundColor.getColor(isDark))
    else Modifier
)

private fun Modifier.styleBackgroundGradient(style: ComponentStyle, isDark: Boolean) = this.then(
    if (style.backgroundGradient != null)
        Modifier.background(Brush.horizontalGradient(style.backgroundGradient.map { Color(if (isDark) it.dark else it.light) }))
    else Modifier
)

internal fun Modifier.styleBorder(
    style: ComponentStyle?,
    isDark: Boolean
) = this.then(
    if (style?.borderWidth != null && style.borderWidth ne 0.0 && style.borderColor != null) {
        Modifier
            .border(style.borderWidth.dp, style.borderColor.getColor(isDark), RoundedCornerShape(style.getCornerRadius()))
            .padding(style.borderWidth.dp)
    } else {
        Modifier
    }
)

internal fun Modifier.styleSize(
    style: ComponentStyle,
    matchParentBox: BoxScope? = null,
    contentMode: ComponentContentMode = ComponentContentMode.FIT,
) = this.then(
    when {
        matchParentBox != null -> with(matchParentBox) { Modifier.matchParentSize() }
        // when style contains both width and height
        style.width != null && style.height != null -> when {
            // fill width in case only width is -1
            style.width eq -1.0 ->
                Modifier
                    .height(style.height.dp)
                    .fillMaxWidth()
            // else we set size with width and height
            else -> Modifier.size(style.width.dp, style.height.dp)
        }
        // if only width is not null, we fill max in case its -1 else we set the width
        style.width != null -> if (style.width eq -1.0) Modifier.fillMaxWidth() else Modifier.width(style.width.dp)
        // if only height is not null, we set the height
        style.height != null ->
            Modifier
                .height(style.height.dp)
                .styleDefaultWidth(contentMode)
        // at the end we fill max width if there is no width/height but the primitive (like image) is set to FILL
        contentMode == ComponentContentMode.FILL -> Modifier.fillMaxWidth()
        else -> Modifier
    }
)

private fun Modifier.styleDefaultWidth(contentMode: ComponentContentMode) = this.then(
    when (contentMode) {
        ComponentContentMode.FILL -> Modifier.fillMaxWidth()
        else -> Modifier
    }
)

internal fun Modifier.styleCorner(style: ComponentStyle) = this.then(
    style.getCornerRadius().let { cornerRadius ->
        when {
            cornerRadius != 0.dp -> Modifier.clip(RoundedCornerShape(cornerRadius))
            else -> Modifier
        }
    }
)

internal fun Modifier.styleShadow(style: ComponentStyle?, isDark: Boolean): Modifier {
    return this.then(
        if (style?.shadow != null) Modifier.coloredShadowRect(
            color = style.shadow.color.getColor(isDark),
            radius = style.shadow.radius.dp,
            cornerRadius = style.getCornerRadius(),
            offsetX = style.shadow.x.dp,
            offsetY = style.shadow.y.dp,
        )
        else Modifier
    )
}

internal fun Modifier.coloredShadowRect(
    color: Color,
    radius: Dp = 0.dp,
    cornerRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) = drawBehind {

    val shadowColor = color.toArgb()
    val transparent = color.copy(alpha = 0.0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent

        frameworkPaint.setShadowLayer(
            radius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )

        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            cornerRadius.toPx(),
            cornerRadius.toPx(),
            paint
        )
    }
}

internal fun Modifier.coloredShadowPath(
    color: Color,
    path: Path,
    radius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp
) = drawBehind {

    val shadowColor = color.toArgb()
    val transparent = color.copy(alpha = 0.0f).toArgb()

    drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparent

        frameworkPaint.setShadowLayer(
            radius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )

        it.drawPath(path, paint)
    }
}

internal data class PrimitiveGestureProperties(
    val actionsDelegate: AppcuesActionsDelegate,
    val actions: Map<UUID, List<Action>>,
    val interactionSource: MutableInteractionSource,
    val indication: Indication?,
    val enabled: Boolean = true,
    val role: Role = Role.Button,
)

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.actions(
    id: UUID,
    gestureProperties: PrimitiveGestureProperties,
    viewDescription: String?,
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
                onLongClick = toLongPressMotionOrNull(gestureProperties.actionsDelegate, viewDescription),
                onClick = toTapMotionOrEmpty(gestureProperties.actionsDelegate, viewDescription),
            )
    }
)

private fun List<Action>.toTapMotionOrEmpty(
    actionsDelegate: AppcuesActionsDelegate,
    viewDescription: String?
): (() -> Unit) {
    // filter only TAP motions
    return filter { it.on == Action.Trigger.TAP }
        // take if there is any
        .takeIf { it.isNotEmpty() }
        // map to ExperienceAction
        ?.map { it.experienceAction }
        // return click block that executes onAction for every ExperienceAction
        // else returns empty click block (notnull property for combinedClickable)
        ?.run {
            {
                actionsDelegate.onActions(
                    actions = this,
                    interactionType = BUTTON_TAPPED,
                    viewDescription = viewDescription
                )
            }
        } ?: { }
}

private fun List<Action>.toLongPressMotionOrNull(
    actionsDelegate: AppcuesActionsDelegate,
    viewDescription: String?
): (() -> Unit)? {
    // filter only LONG_PRESS motions
    return filter { it.on == Action.Trigger.LONG_PRESS }
        // take if there is any
        .takeIf { it.isNotEmpty() }
        // map to ExperienceAction
        ?.map { it.experienceAction }
        // return click block that executes onAction for every ExperienceAction
        // else returns null
        ?.run {
            {
                actionsDelegate.onActions(
                    actions = this,
                    interactionType = BUTTON_LONG_PRESSED,
                    viewDescription = viewDescription
                )
            }
        }
}

internal fun Modifier.imageAspectRatio(
    intrinsicSize: ComponentSize?,
    stackScope: StackScope,
    style: ComponentStyle,
    density: Density,
    contentMode: ComponentContentMode = ComponentContentMode.FILL
) = this.then(
    // apply aspectRatio only when intrinsicSize is not null or any values is bigger than 0
    if (intrinsicSize != null && (intrinsicSize.width > 0 && intrinsicSize.height > 0)) {
        Modifier.appcuesAspectRatio(
            ratio = intrinsicSize.width.toFloat() / intrinsicSize.height.toFloat(),
            contentMode = contentMode,
            stackScope = stackScope,
            // when we have size we should subtract the padding to know image's true size
            widthPixels = style.getImageWidthPixels(density),
            heightPixels = style.getImageHeightPixels(density),
        )
    } else Modifier
)

private fun ComponentStyle.getImageWidthPixels(density: Density) = with(density) {
    // get true image width by subtracting current width with existing
    // padding (leading and trailing) and borderWidth times 2 (as it applies on both sides)
    val horizontalPadding = (paddingLeading ?: 0.0) + (paddingTrailing ?: 0.0)
    width?.let { (it - (horizontalPadding + (borderWidth ?: 0.0) * 2)).dp.toPx() }
}

private fun ComponentStyle.getImageHeightPixels(density: Density) = with(density) {
    // get true image height by subtracting current height with existing
    // padding (top and bottom) and borderWidth times 2 (as it applies on both sides)
    val verticalPadding = (paddingTop ?: 0.0) + (paddingBottom ?: 0.0)
    height?.let { (it - (verticalPadding + (borderWidth ?: 0.0) * 2)).dp.toPx() }
}

internal fun Modifier.appcuesAspectRatio(
    ratio: Float,
    contentMode: ComponentContentMode,
    stackScope: StackScope,
    widthPixels: Float?,
    heightPixels: Float?,
) =
    this.then(
        AppcuesAspectRatioModifier(
            originalAspectRatio = ratio,
            contentMode = contentMode,
            stackScope = stackScope,
            widthPixels = widthPixels,
            heightPixels = heightPixels,
        )
    )
