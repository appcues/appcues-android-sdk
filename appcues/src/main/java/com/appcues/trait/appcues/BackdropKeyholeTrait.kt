package com.appcues.trait.appcues

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.CIRCLE
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.RECTANGLE
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR
import com.appcues.trait.appcues.TargetRectangleTrait.Companion.TARGET_RECTANGLE_METADATA
import com.appcues.trait.appcues.TargetRectangleTrait.TargetRectangleInfo
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

internal class BackdropKeyholeTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/backdrop-keyhole"
    }

    private enum class ConfigShape {
        RECTANGLE, CIRCLE
    }

    private val shape = when (config.getConfig<String>("shape")) {
        "circle" -> CIRCLE
        "rectangle" -> RECTANGLE
        else -> RECTANGLE
    }

    private val configCornerRadius = config.getConfig<Double>("cornerRadius") ?: 0.0

    private val configSpreadRadius = config.getConfig<Double>("spreadRadius") ?: 0.0

    private val configBlurRadius = config.getConfig<Double>("blurRadius") ?: 0.0

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val shapeBlurRadius = if (shape == CIRCLE) configBlurRadius.toFloat() else 0.0f
        val rect = rememberMetadataRect(metadata, configSpreadRadius.toFloat())
        val floatAnimation = rememberMetadataFloatAnimation(metadata)
        val encompassesDiameter = getRectEncompassesRadius(rect.width, rect.height, shapeBlurRadius) * 2

        // animated values
        val xPosition = animateXPositionAsState(rect, encompassesDiameter, floatAnimation)
        val yPosition = animateYPositionAsState(rect, encompassesDiameter, floatAnimation)
        val width = animateFloatAsState(if (shape == RECTANGLE) rect.width else encompassesDiameter, floatAnimation)
        val height = animateFloatAsState(if (shape == RECTANGLE) rect.height else encompassesDiameter, floatAnimation)
        val cornerRadius = animateFloatAsState(getCornerRadius(rect.width, rect.height, shapeBlurRadius), floatAnimation)
        val blurRadius = animateFloatAsState(shapeBlurRadius, floatAnimation)
        val encompassesRadiusPx = animateFloatAsState(with(density) { encompassesDiameter.dp.toPx() / 2 }, floatAnimation)

        // pixel values
        val sizePx = with(density) { Size(width.value.dp.toPx(), height.value.dp.toPx()) }
        val positionPx = with(density) { Offset(xPosition.value.dp.toPx(), yPosition.value.dp.toPx()) }
        val blurRadiusPx = with(density) { blurRadius.value.dp.toPx() }
        val rectCornerRadius = with(density) { CornerRadius(cornerRadius.value.dp.toPx()) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                // adds a graphic layer to ensure that any BlendModes will work as expected
                .graphicsLayer(alpha = 0.99F)
                .drawWithContent {
                    drawContent()

                    val shapeCenter = Offset(positionPx.x + sizePx.width / 2, positionPx.y + sizePx.height / 2)
                    val blurStartPoint = ((sizePx.width / 2) - blurRadiusPx) / (sizePx.width / 2)

                    drawRoundRect(
                        topLeft = positionPx,
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(blurStartPoint to Color.Transparent, 1.0f to Color.Black),
                            center = shapeCenter,
                            radius = encompassesRadiusPx.value
                        ),
                        size = sizePx,
                        cornerRadius = rectCornerRadius,
                        blendMode = BlendMode.DstIn
                    )
                }
        ) {
            content()
        }
    }

    @Composable
    private fun animateXPositionAsState(rect: Rect, encompassesDiameter: Float, animationSpec: AnimationSpec<Float>): State<Float> {
        val circleXOffset = (encompassesDiameter - rect.width) / 2

        return animateFloatAsState(
            targetValue = if (shape == RECTANGLE) rect.left else rect.left - circleXOffset,
            animationSpec = animationSpec
        )
    }

    @Composable
    private fun animateYPositionAsState(rect: Rect, encompassesDiameter: Float, animationSpec: AnimationSpec<Float>): State<Float> {
        val circleYOffset = (encompassesDiameter - rect.height) / 2

        return animateFloatAsState(
            targetValue = if (shape == RECTANGLE) rect.top else rect.top - circleYOffset,
            animationSpec = animationSpec
        )
    }

    @Composable
    private fun rememberMetadataRect(metadata: AppcuesStepMetadata, spreadRadius: Float): Rect {
        val windowInfo = rememberAppcuesWindowInfo()
        return remember(metadata) {
            val rectInfo = (metadata.actual[TARGET_RECTANGLE_METADATA] as TargetRectangleInfo?) ?: TargetRectangleInfo()
            val screenWidth = windowInfo.widthDp.value
            val screenHeight = windowInfo.heightDp.value

            Rect(
                offset = Offset(
                    x = (screenWidth * rectInfo.relativeX).toFloat() + rectInfo.x - spreadRadius,
                    y = (screenHeight * rectInfo.relativeY).toFloat() + rectInfo.y - spreadRadius,
                ),
                size = Size(
                    width = (screenWidth * rectInfo.relativeWidth).toFloat() + rectInfo.width + (spreadRadius * 2),
                    height = (screenHeight * rectInfo.relativeHeight).toFloat() + rectInfo.height + (spreadRadius * 2),
                )
            )
        }
    }

    @Composable
    private fun rememberMetadataFloatAnimation(metadata: AppcuesStepMetadata): TweenSpec<Float> {
        return remember(metadata) {
            val duration = (metadata.actual[StepAnimationTrait.METADATA_ANIMATION_DURATION] as Int?) ?: StepAnimationTrait.DEFAULT_ANIMATION
            when ((metadata.actual[StepAnimationTrait.METADATA_ANIMATION_EASING] as StepAnimationEasing?)) {
                LINEAR -> tween(durationMillis = duration, easing = LinearEasing)
                EASE_IN -> tween(durationMillis = duration, easing = EaseIn)
                EASE_OUT -> tween(durationMillis = duration, easing = EaseOut)
                EASE_IN_OUT -> tween(durationMillis = duration, easing = EaseInOut)
                // animation with no duration is the easiest way to not use animation here
                null -> tween(durationMillis = 0, easing = LinearEasing)
            }
        }
    }

    private fun getCornerRadius(width: Float, height: Float, blurRadius: Float): Float {
        return when (shape) {
            RECTANGLE -> configCornerRadius.toFloat()
            CIRCLE -> (getRectEncompassesRadius(width, height, blurRadius))
                // calculate Radius that encompasses the rect
                .let { ((it * Math.PI) / 2).toFloat() }
        }
    }

    private fun getRectEncompassesRadius(width: Float, height: Float, blurRadius: Float): Float {
        return max((sqrt(width.pow(2) + height.pow(2)) / 2) + blurRadius, 0f)
    }
}
