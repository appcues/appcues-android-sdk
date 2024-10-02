package com.appcues.trait.appcues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.appcues.AppcuesCoroutineScope
import com.appcues.R
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getColor
import com.appcues.ui.extensions.getMargins
import com.appcues.ui.extensions.xShapePath
import com.appcues.ui.utils.margin
import com.appcues.util.ne
import kotlinx.coroutines.launch
import kotlin.math.min

internal class SkippableTrait(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experienceRenderer: ExperienceRenderer,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) : ContainerDecoratingTrait, BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/skippable"

        private const val CONFIG_BUTTON_APPEARANCE = "buttonAppearance"
        private const val CONFIG_BUTTON_STYLE = "buttonStyle"
        private const val CONFIG_IGNORE_BACKDROP_TAP = "ignoreBackdropTap"

        // default button is 30x30 - can override in buttonStyle
        private const val BUTTON_DEFAULT_SIZE = 30.0

        // default margin of 8 around every side - can override in buttonStyle
        private const val BUTTON_DEFAULT_MARGIN = 8

        // the 'X' drawn in the button will scale to 40% of button size (height)
        private const val BUTTON_X_SCALE_FACTOR = 0.4

        // the 'X' in a minimal style button uses a 1.5 dp stroke width for a 30 dp size button
        // and scales accordingly with different sizes
        private const val BUTTON_X_MINIMAL_STROKE_FACTOR = 1.5

        // the 'X' in a default style button uses a 2.0 dp stroke width for a 30 dp size button
        // and scales accordingly with different sizes
        private const val BUTTON_X_DEFAULT_STOKE_FACTOR = 2.0
    }

    private enum class ButtonAppearance {
        DEFAULT, MINIMAL, HIDDEN
    }

    override val containerComposeOrder = ContainerDecoratingType.OVERLAY

    // config properties

    private val buttonStyle = config.getConfigStyle(CONFIG_BUTTON_STYLE)
    private val ignoreBackdropTap = config.getConfigOrDefault(CONFIG_IGNORE_BACKDROP_TAP, false)
    private val buttonAppearance = when (config.getConfig<String>(CONFIG_BUTTON_APPEARANCE)) {
        "hidden" -> ButtonAppearance.HIDDEN
        "minimal" -> ButtonAppearance.MINIMAL
        "default" -> ButtonAppearance.DEFAULT
        else -> ButtonAppearance.DEFAULT
    }

    val allowDismissal: Boolean
        get() = !ignoreBackdropTap

    // computed style props

    private val buttonWidth by lazy { buttonStyle?.width ?: buttonStyle?.height ?: BUTTON_DEFAULT_SIZE }
    private val buttonHeight by lazy { buttonStyle?.height ?: BUTTON_DEFAULT_SIZE }
    private val defaultCornerRadius by lazy { min(buttonWidth, buttonHeight) / 2 }
    private val horizontalAlignment by lazy { buttonStyle?.horizontalAlignment ?: ComponentHorizontalAlignment.TRAILING }
    private val verticalAlignment by lazy { buttonStyle?.verticalAlignment ?: ComponentVerticalAlignment.TOP }
    private val cornerRadius by lazy { buttonStyle?.cornerRadius ?: defaultCornerRadius }
    private val rippleRadius by lazy { buttonSize / 2 }
    private val strokeWidth by lazy {
        val strokeFactor = when (buttonAppearance) {
            ButtonAppearance.MINIMAL -> BUTTON_X_MINIMAL_STROKE_FACTOR
            else -> BUTTON_X_DEFAULT_STOKE_FACTOR
        }
        (strokeFactor / BUTTON_DEFAULT_SIZE * buttonSize).dp
    }
    private val buttonSize
        // normally height and width are equal, but for any calculations where a single
        // size value is needed, height is the proxy for size
        get() = buttonHeight

    @Composable
    override fun BoxScope.DecorateContainer(containerPadding: PaddingValues, safeAreaInsets: PaddingValues) {
        val description = stringResource(id = R.string.appcues_skippable_trait_dismiss)

        if (buttonAppearance != ButtonAppearance.HIDDEN) {
            Spacer(
                modifier = Modifier
                    .padding(safeAreaInsets)
                    .align(getBoxAlignment(horizontalAlignment, verticalAlignment))
                    .margin(buttonStyle.getMargins(BUTTON_DEFAULT_MARGIN.dp))
                    .styleButton(isSystemInDarkTheme())
                    // useful for testing and also for accessibility
                    .semantics { this.contentDescription = description }
            )
        }
    }

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        if (ignoreBackdropTap.not()) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    // add click listener but without any ripple effect.
                    .pointerInput(Unit) {
                        detectTapGestures {
                            appcuesCoroutineScope.launch {
                                experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = false)
                            }
                        }
                    },
            )
        }

        content()
    }

    // styles the close X button differently based on the appearance set in config
    private fun Modifier.styleButton(isDark: Boolean): Modifier {
        return this.then(
            when (buttonAppearance) {
                ButtonAppearance.HIDDEN -> Modifier
                ButtonAppearance.DEFAULT ->
                    Modifier
                        .buttonShadow(isDark)
                        .width(buttonWidth.dp)
                        .height(buttonHeight.dp)
                        .clip(RoundedCornerShape(cornerRadius.dp))
                        .handleSkipClick()
                        .drawDefaultX(isDark)
                        .buttonBorder(isDark)
                ButtonAppearance.MINIMAL ->
                    Modifier
                        .width(buttonWidth.dp)
                        .height(buttonHeight.dp)
                        .handleSkipClick()
                        .drawMinimalX(isDark)
            }
        )
    }

    // default X button
    // - background shape with a background color from style or a default semi-transparent 0x54000000
    // - foreground X path with color from style or a default 0xFFEFEFEF
    private fun Modifier.drawDefaultX(isDark: Boolean): Modifier {
        return this.then(
            background(buttonStyle?.backgroundColor?.getColor(isDark) ?: Color(color = 0x54000000))
                .drawBehind {
                    xShapePath(pathSize = (buttonHeight * BUTTON_X_SCALE_FACTOR).dp).also {
                        drawPath(
                            path = it,
                            color = buttonStyle?.foregroundColor?.getColor(isDark) ?: Color(color = 0xFFEFEFEF),
                            style = Stroke(strokeWidth.toPx()),
                        )
                    }
                }
        )
    }

    // minimal X button
    // - no background
    // - foreground color uses 70% white tint with an exclusion blend by default, but a specific color from style can override
    // - an optional shadow can be applied to the path of the X itself (not a background container shadow like default)
    private fun Modifier.drawMinimalX(isDark: Boolean): Modifier {
        return this.then(
            drawBehind {
                xShapePath(pathSize = (buttonHeight * BUTTON_X_SCALE_FACTOR).dp).also {
                    drawPathShadow(it, isDark)
                    val foregroundColor = buttonStyle?.foregroundColor?.getColor(isDark)
                    val drawStyle = Stroke(strokeWidth.toPx())
                    if (foregroundColor != null) {
                        drawPath(
                            path = it,
                            color = foregroundColor,
                            style = drawStyle
                        )
                    } else {
                        drawPath(
                            path = it,
                            color = Color.Black,
                            colorFilter = ColorFilter.tint(Color(color = 0xB3FFFFFF), BlendMode.Difference),
                            style = drawStyle
                        )
                    }
                }
            }
        )
    }

    // draws a shadow on the outer button shape - only used in default appearance
    private fun Modifier.buttonShadow(isDark: Boolean): Modifier {
        return this.then(
            buttonStyle?.shadow?.let { shadow ->
                drawBehind {
                    val color = shadow.color.getColor(isDark)
                    val shadowColor = color.toArgb()
                    val transparent = color.copy(alpha = 0.0f).toArgb()

                    drawIntoCanvas {
                        val paint = Paint()
                        val frameworkPaint = paint.asFrameworkPaint()
                        frameworkPaint.color = transparent

                        frameworkPaint.setShadowLayer(
                            shadow.radius.dp.toPx(),
                            shadow.x.dp.toPx(),
                            shadow.y.dp.toPx(),
                            shadowColor
                        )

                        it.drawRoundRect(
                            0f,
                            0f,
                            this.size.width,
                            this.size.height,
                            cornerRadius.dp.toPx(),
                            cornerRadius.dp.toPx(),
                            paint
                        )
                    }
                }
            } ?: Modifier
        )
    }

    // draws a border around the outer button shape - only used in default appearance
    private fun Modifier.buttonBorder(isDark: Boolean) = this.then(
        if (buttonStyle?.borderWidth != null && buttonStyle.borderWidth ne 0.0 && buttonStyle.borderColor != null) {
            Modifier
                .border(
                    width = buttonStyle.borderWidth.dp,
                    color = buttonStyle.borderColor.getColor(isDark),
                    shape = RoundedCornerShape(cornerRadius.dp)
                )
                .padding(buttonStyle.borderWidth.dp)
        } else {
            Modifier
        }
    )

    // dismiss the experience on button tap
    private fun Modifier.handleSkipClick(): Modifier {
        return this.then(
            Modifier.composed {
                clickable(
                    onClick = {
                        appcuesCoroutineScope.launch {
                            experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = false)
                        }
                    },
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = rippleRadius.dp),
                    onClickLabel = stringResource(id = R.string.appcues_skippable_trait_dismiss)
                )
            }
        )
    }

    // used to draw a shadow under the 'X' in minimal appearance
    private fun DrawScope.drawPathShadow(path: Path, isDark: Boolean) {
        buttonStyle?.shadow?.let { shadow ->
            val color = shadow.color.getColor(isDark)
            val shadowColor = color.toArgb()
            val transparent = color.copy(alpha = 0.0f).toArgb()

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.style = PaintingStyle.Stroke
                    this.strokeWidth = this@SkippableTrait.strokeWidth.toPx()
                }
                paint.asFrameworkPaint().apply {
                    this.color = transparent
                    setShadowLayer(
                        shadow.radius.dp.toPx(),
                        shadow.x.dp.toPx(),
                        shadow.y.dp.toPx(),
                        shadowColor
                    )
                }
                canvas.drawPath(path, paint)
            }
        }
    }
}
