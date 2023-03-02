package com.appcues.trait.appcues

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.appcues.trait.extensions.getRect
import com.appcues.trait.extensions.rememberFloatStepAnimation
import com.appcues.trait.extensions.rememberTargetRectangleInfo
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
        val targetRectInfo = rememberTargetRectangleInfo(metadata)
        val targetRect = targetRectInfo.getRect(rememberAppcuesWindowInfo()).inflateOrEmpty(configSpreadRadius)
        val floatAnimation = rememberFloatStepAnimation(metadata)
        val encompassesDiameter = getRectEncompassesRadius(targetRect.width, targetRect.height, shapeBlurRadius) * 2

        // animated values
        val xPosition = animateXPositionAsState(targetRect, encompassesDiameter, floatAnimation)
        val yPosition = animateYPositionAsState(targetRect, encompassesDiameter, floatAnimation)
        val width = animateFloatAsState(if (shape == RECTANGLE) targetRect.width else encompassesDiameter, floatAnimation)
        val height = animateFloatAsState(if (shape == RECTANGLE) targetRect.height else encompassesDiameter, floatAnimation)
        val cornerRadius = animateFloatAsState(getCornerRadius(targetRect.width, targetRect.height, shapeBlurRadius), floatAnimation)
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

    private fun Rect?.inflateOrEmpty(spreadRadius: Double): Rect {
        return this?.inflate(spreadRadius.toFloat()) ?: Rect(Offset(0f, 0f), Size(0f, 0f))
    }
}
