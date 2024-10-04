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
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.CIRCLE
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.RECTANGLE
import com.appcues.trait.extensions.getRect
import com.appcues.trait.extensions.getRectEncompassesRadius
import com.appcues.trait.extensions.inflateOrEmpty
import com.appcues.trait.extensions.rememberFloatStepAnimation
import com.appcues.trait.extensions.rememberTargetRectangleInfo
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.utils.rememberAppcuesWindowInfo

internal class BackdropKeyholeTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait, MetadataSettingTrait {

    companion object {

        const val TYPE = "@appcues/backdrop-keyhole"

        const val METADATA_KEYHOLE_SETTINGS = "keyholeSettings"
    }

    override val isBlocking = false

    enum class ConfigShape {
        RECTANGLE, CIRCLE
    }

    data class KeyholeSettings(
        val cornerRadius: Double,
        val spreadRadius: Double,
        val blurRadius: Double,
        val shape: ConfigShape,
    )

    private val keyholeSettings = KeyholeSettings(
        cornerRadius = config.getConfig<Double>("cornerRadius") ?: 0.0,
        spreadRadius = config.getConfig<Double>("spreadRadius") ?: 0.0,
        blurRadius = config.getConfig<Double>("blurRadius") ?: 0.0,
        shape = getConfigShape()
    )

    private fun getConfigShape(): ConfigShape {
        return when (config.getConfig<String>("shape")) {
            "circle" -> CIRCLE
            "rectangle" -> RECTANGLE
            else -> RECTANGLE
        }
    }

    override fun produceMetadata(): Map<String, Any?> {
        return hashMapOf(METADATA_KEYHOLE_SETTINGS to keyholeSettings)
    }

    @Composable
    override fun BoxScope.BackdropDecorate(isBlocking: Boolean, content: @Composable BoxScope.() -> Unit) {
        // if there is no blocking backdrop (backdrop trait) then we do not decorate any keyhole sections
        if (!isBlocking) {
            content()
            return
        }

        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val targetRectInfo = rememberTargetRectangleInfo(metadata)

        val shapeBlurRadius = if (keyholeSettings.shape == CIRCLE) keyholeSettings.blurRadius.toFloat() else 0.0f
        val targetRect = targetRectInfo.getRect(rememberAppcuesWindowInfo()).inflateOrEmpty(keyholeSettings.spreadRadius)
        val floatAnimation = rememberFloatStepAnimation(metadata)
        val encompassesDiameter = targetRect.getRectEncompassesRadius(shapeBlurRadius) * 2

        // animated values
        val xPosition = animateXPositionAsState(targetRect, encompassesDiameter, floatAnimation)
        val yPosition = animateYPositionAsState(targetRect, encompassesDiameter, floatAnimation)
        val width = animateFloatAsState(if (keyholeSettings.shape == RECTANGLE) targetRect.width else encompassesDiameter, floatAnimation)
        val height = animateFloatAsState(if (keyholeSettings.shape == RECTANGLE) targetRect.height else encompassesDiameter, floatAnimation)
        val cornerRadius = animateFloatAsState(targetRect.getCornerRadius(shapeBlurRadius), floatAnimation)
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

                    if (encompassesRadiusPx.value > 0) {
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
                }
        ) {
            content()
        }
    }

    @Composable
    private fun animateXPositionAsState(rect: Rect, encompassesDiameter: Float, animationSpec: AnimationSpec<Float>): State<Float> {
        val circleXOffset = (encompassesDiameter - rect.width) / 2

        return animateFloatAsState(
            targetValue = if (keyholeSettings.shape == RECTANGLE) rect.left else rect.left - circleXOffset,
            animationSpec = animationSpec
        )
    }

    @Composable
    private fun animateYPositionAsState(rect: Rect, encompassesDiameter: Float, animationSpec: AnimationSpec<Float>): State<Float> {
        val circleYOffset = (encompassesDiameter - rect.height) / 2

        return animateFloatAsState(
            targetValue = if (keyholeSettings.shape == RECTANGLE) rect.top else rect.top - circleYOffset,
            animationSpec = animationSpec
        )
    }

    private fun Rect.getCornerRadius(blurRadius: Float): Float {
        return when (keyholeSettings.shape) {
            RECTANGLE -> keyholeSettings.cornerRadius.toFloat()
            CIRCLE -> (getRectEncompassesRadius(blurRadius))
                // calculate Radius that encompasses the rect
                .let { ((it * Math.PI) / 2).toFloat() }
        }
    }
}
