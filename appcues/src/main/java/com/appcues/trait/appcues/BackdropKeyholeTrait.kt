package com.appcues.trait.appcues

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigInt
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.CIRCLE
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.RECTANGLE
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesStepMetadata
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

    private val emptyRect = Rect(0f, 0f, 0f, 0f)

    private val shape = when (config.getConfig<String>("shape")) {
        "circle" -> CIRCLE
        else -> RECTANGLE
    }

    private val rectCornerRadius = config.getConfigInt("cornerRadius") ?: 0

    private val spreadRadius = config.getConfigInt("spreadRadius") ?: 0

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val actualRect = rememberMetadataRect(metadata)
        val animation = rememberMetadataAnimation(metadata)

        val circleSize = getRectEncompassesRadius(actualRect.width, actualRect.height) * 2
        val circleEncompassXOffset = (circleSize - actualRect.width) / 2
        val circleEncompassYOffset = (circleSize - actualRect.height) / 2
        val xPosition = animateFloatAsState(
            targetValue = if (shape == RECTANGLE) actualRect.left else actualRect.left - circleEncompassXOffset,
            animationSpec = animation
        )
        val yPosition = animateFloatAsState(
            targetValue = if (shape == RECTANGLE) actualRect.top else actualRect.top - circleEncompassYOffset,
            animationSpec = animation
        )
        val width = animateFloatAsState(if (shape == RECTANGLE) actualRect.width else circleSize, animationSpec = animation)
        val height = animateFloatAsState(if (shape == RECTANGLE) actualRect.height else circleSize, animationSpec = animation)
        val cornerRadius = animateFloatAsState(getCornerRadius(actualRect.width, actualRect.height), animationSpec = animation)

        Box(
            modifier = Modifier
                .fillMaxSize()
                // draw the desired shape as a clipPath
                .drawWithContent {
                    val size = with(density) { Size(width.value.dp.toPx(), height.value.dp.toPx()) }
                    val position = with(density) { Offset(xPosition.value.dp.toPx(), yPosition.value.dp.toPx()) }
                    clipPath(
                        path = Path().apply {
                            addOutline(
                                RoundedCornerShape(cornerRadius.value.dp)
                                    .createOutline(size, layoutDirection, density)
                            )
                            translate(position)
                        },
                        clipOp = ClipOp.Difference,
                    ) {
                        this@drawWithContent.drawContent()
                    }
                }

        ) {
            content()
        }
    }

    @Composable
    private fun rememberMetadataRect(metadata: AppcuesStepMetadata): Rect {
        val actualRect = remember(metadata) {
            var rect = (metadata.actual[TargetElementTrait.METADATA_TARGET_RECT] as Rect?) ?: emptyRect
            if (spreadRadius != 0) {
                rect = Rect(
                    offset = Offset(rect.topLeft.x - spreadRadius, rect.topLeft.y - spreadRadius),
                    size = Size(width = rect.width + (spreadRadius * 2), height = rect.height + (spreadRadius * 2))
                )
            }
            rect
        }
        return actualRect
    }

    @Composable
    private fun rememberMetadataAnimation(metadata: AppcuesStepMetadata): TweenSpec<Float> {
        val animation = remember<TweenSpec<Float>>(metadata) {
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
        return animation
    }

    private fun getCornerRadius(width: Float, height: Float): Float {
        return when (shape) {
            RECTANGLE -> rectCornerRadius.toFloat()
            CIRCLE -> {
                getRectEncompassesRadius(width, height).let {
                    ((it * Math.PI) / 2).toFloat()
                }
            }
        }
    }

    private fun getRectEncompassesRadius(width: Float, height: Float): Float {
        return sqrt(width.pow(2) + height.pow(2)) / 2
    }
}
