package com.appcues.trait.appcues

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_TAPPED
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigActions
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.extensions.getRect
import com.appcues.trait.extensions.rememberTargetRectangleInfo
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.extensions.toLongPressMotionOrNull
import com.appcues.ui.extensions.toTapMotionOrEmpty
import com.appcues.ui.utils.rememberAppcuesWindowInfo

internal class TargetInteractionTrait(
    override val config: AppcuesConfigMap,
    private val actionProcessor: ActionProcessor,
    actionRegistry: ActionRegistry,
) : BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/target-interaction"

        private const val VIEW_DESCRIPTION = "Target Rectangle"
    }

    private val actions = config.getConfigActions("actions", actionRegistry)

    private val actionDelegate = object : AppcuesActionsDelegate {
        override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
            actionProcessor.process(actions, interactionType, viewDescription)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        // calling content before our composable makes so we are on top of all other backdrop traits
        // wrapping content call
        content()

        val targetRectInfo = rememberTargetRectangleInfo(LocalAppcuesStepMetadata.current)
        // only draws when target rectangle info exists
        targetRectInfo.getRect(rememberAppcuesWindowInfo())?.let { rect ->
            val sizeDp = DpSize(rect.width.dp, rect.height.dp)
            val offsetDp = DpOffset(rect.left.dp, rect.top.dp)

            Box(
                modifier = Modifier
                    .size(sizeDp)
                    .offset(x = offsetDp.x, y = offsetDp.y)
                    // add click listener but without any ripple effect.
                    .then(
                        if (actions.isNotEmpty()) {
                            Modifier.combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onLongClick = actions.toLongPressMotionOrNull(actionDelegate, TARGET_LONG_PRESSED, VIEW_DESCRIPTION),
                                onClick = actions.toTapMotionOrEmpty(actionDelegate, TARGET_TAPPED, VIEW_DESCRIPTION),
                            )
                        }
                        // when no actions are listed, the default behavior is to
                        else Modifier.pointerInput(Unit) { detectTapGestures { } }
                    )
            )
        }
    }
}
