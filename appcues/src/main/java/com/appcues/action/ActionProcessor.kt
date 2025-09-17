package com.appcues.action

import com.appcues.Appcues
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.action.appcues.StepInteractionAction.StepInteractionData
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.RenderContext
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class ActionProcessor(override val scope: AppcuesScope) : AppcuesComponent {

    // lazy initialization injection to avoid circular dependency
    private val appcues: Appcues by inject()
    private val appcuesCoroutineScope: CoroutineScope by inject()

    private val actionQueue = Channel<ExperienceAction>(Channel.UNLIMITED)

    init {
        appcuesCoroutineScope.launch {
            for (action in actionQueue) {
                action.execute()
            }
        }
    }

    // This is used for internal actions, like navigation actions processed during step transitions.
    suspend fun process(actions: List<ExperienceAction>) {
        if (actions.isEmpty()) return

        // These actions are executed directly, not placed in the actionQueue Channel.
        // The reason is that they occur inside of the context of another action already executing,
        // typically the Continue action moving to the next step. Since we need to be able to
        // make this a suspend function and wait on them, we cannot place them in the queue or
        // suspend the currently executing action at all, or the queue would become deadlocked.
        transformQueue(actions).forEach { it.execute() }
    }

    // This is used for post flow actions, ensures we never get to a deadlock state with the
    // current transition being processed by the state machine. Mainly because of LaunchExperienceAction when we have another
    // experience we would want to trigger when current flow completes
    fun enqueue(actions: List<ExperienceAction>) {
        transformQueue(actions).forEach { actionQueue.trySend(it) }
    }

    // Enqueue single action (no transformQueue necessary)
    fun enqueue(action: ExperienceAction) {
        actionQueue.trySend(action)
    }

    // This version is used by the viewModel to enqueue interactive actions from user input - button taps.
    // It includes relevant information about the user interaction that can be used in the auto generated
    // step_interaction event.
    fun enqueue(
        renderContext: RenderContext,
        actions: List<ExperienceAction>,
        interactionType: InteractionType,
        viewDescription: String?
    ) {
        if (actions.isEmpty()) return

        // the stepInteraction is not included in the transform, and is inserted at the front of queue
        transformQueue(actions).toMutableList()
            .apply { add(0, getStepInteractionAction(renderContext, actions, interactionType, viewDescription)) }
            // user interactions are processed through the shared queue, to ensure consistency in order of operations
            .forEach { actionQueue.trySend(it) }
    }

    private fun transformQueue(actions: List<ExperienceAction>) = actions.fold(actions) { currentQueue, action ->
        val indexInCurrent = currentQueue.indexOfFirst { it === action }
        val transformingAction = action as? ExperienceActionQueueTransforming
        if (indexInCurrent != -1 && transformingAction != null) {
            transformingAction.transformQueue(currentQueue, indexInCurrent, appcues)
        } else {
            currentQueue
        }
    }

    private fun getStepInteractionAction(
        renderContext: RenderContext,
        actions: List<ExperienceAction>,
        interactionType: InteractionType,
        viewDescription: String?
    ): StepInteractionAction {
        // reverse the action list to get the first from back to front
        val primaryAction = actions.lastOrNull { it is MetadataSettingsAction } as? MetadataSettingsAction

        // get an instance of StepInteractionAction so it can access
        // StateMachine and AnalyticsTracker
        val data = StepInteractionData(
            interactionType = interactionType,
            viewDescription = viewDescription,
            category = primaryAction?.category ?: "",
            destination = primaryAction?.destination ?: "",
        )

        return StepInteractionAction(renderContext, data, get(), get())
    }
}
