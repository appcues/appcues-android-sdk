package com.appcues.trait.appcues

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.dp
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_TAPPED
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigActions
import com.appcues.data.model.getConfigOrDefault
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.CIRCLE
import com.appcues.trait.appcues.BackdropKeyholeTrait.ConfigShape.RECTANGLE
import com.appcues.trait.appcues.BackdropKeyholeTrait.KeyholeSettings
import com.appcues.trait.extensions.getRect
import com.appcues.trait.extensions.getRectEncompassesRadius
import com.appcues.trait.extensions.inflateOrEmpty
import com.appcues.trait.extensions.rememberTargetRectangleInfo
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesTapForwardingDelegate
import com.appcues.ui.extensions.toLongPressMotionOrNull
import com.appcues.ui.extensions.toTapMotionOrEmpty
import com.appcues.ui.utils.rememberAppcuesWindowInfo

internal class TargetInteractionTrait(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val actionProcessor: ActionProcessor,
    actionRegistry: ActionRegistry,
) : BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/target-interaction"

        private const val VIEW_DESCRIPTION = "Target Rectangle"
    }

    override val isBlocking = false

    private val actions = config.getConfigActions("actions", renderContext, actionRegistry)
    private val enablePassThrough = config.getConfigOrDefault("enablePassThrough", false)

    private val actionDelegate = object : AppcuesActionsDelegate {
        override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
            actionProcessor.enqueue(renderContext, actions, interactionType, viewDescription)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BoxScope.BackdropDecorate(isBlocking: Boolean, content: @Composable BoxScope.() -> Unit) {
        // calling content before our composable makes so we are on top of all other backdrop traits
        // wrapping content call
        content()

        // if there is no blocking backdrop (no backdrop trait) - then interactions on the backdrop simply
        // flow through to the underlying application. We do not apply a target interaction since it would result
        // in an invisible section of the app capturing and taking action on touch in an unexpected way.
        if (!isBlocking) return

        val tapForwardingDelegate = LocalAppcuesTapForwardingDelegate.current

        val targetRectInfo = rememberTargetRectangleInfo(LocalAppcuesStepMetadata.current)
        val keyholeSettings = rememberKeyholeSettings(LocalAppcuesStepMetadata.current)
        // only draws when target rectangle info exists
        targetRectInfo.getRect(rememberAppcuesWindowInfo())?.let {
            val rect = it.inflateOrEmpty(keyholeSettings.spreadRadius)
            val shapeBlurRadius = if (keyholeSettings.shape == CIRCLE) keyholeSettings.blurRadius.toFloat() else 0.0f
            val encompassesDiameter = rect.getRectEncompassesRadius(shapeBlurRadius) * 2
            val width = if (keyholeSettings.shape == RECTANGLE) rect.width else encompassesDiameter
            val height = if (keyholeSettings.shape == RECTANGLE) rect.height else encompassesDiameter
            val offsetX = if (keyholeSettings.shape == RECTANGLE) rect.left else rect.left - ((encompassesDiameter - rect.width) / 2)
            val offsetY = if (keyholeSettings.shape == RECTANGLE) rect.top else rect.top - ((encompassesDiameter - rect.height) / 2)
            val clipShape = if (keyholeSettings.shape == RECTANGLE) RoundedCornerShape(keyholeSettings.cornerRadius.dp) else CircleShape

            var globalOffset by remember { mutableStateOf(Offset.Zero) }

            Box(
                modifier = Modifier
                    .size(width = width.dp, height = height.dp)
                    .offset(x = offsetX.dp, y = offsetY.dp)
                    .clip(clipShape)
                    .onGloballyPositioned { coordinates ->
                        globalOffset = coordinates.positionInWindow()
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                if (enablePassThrough) {
                                    // If a target is allowed to pass touches through to the host application,
                                    // we find the global offset of the tap and pass it back through to the
                                    // View that is hosting our experience, using this tapForwardingDelegate.
                                    // This will end up passing MotionEvents back through to the underlying views
                                    // as if the tap was not captured at all by our Composable here.
                                    val globalTapOffset = Offset(
                                        x = offset.x + globalOffset.x,
                                        y = offset.y + globalOffset.y
                                    )
                                    tapForwardingDelegate.onTap(globalTapOffset)
                                }
                                actions.toTapMotionOrEmpty(actionDelegate, TARGET_TAPPED, VIEW_DESCRIPTION).invoke()
                            },
                            onLongPress = {
                                actions.toLongPressMotionOrNull(actionDelegate, TARGET_LONG_PRESSED, VIEW_DESCRIPTION)?.invoke()
                            }
                        )
                    }
            )
        }
    }

    @Composable
    internal fun rememberKeyholeSettings(metadata: AppcuesStepMetadata): KeyholeSettings {
        return (metadata.current[BackdropKeyholeTrait.METADATA_KEYHOLE_SETTINGS] as KeyholeSettings?)
            ?: KeyholeSettings(0.0, 0.0, 0.0, RECTANGLE)
    }
}
