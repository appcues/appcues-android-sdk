package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.action.appcues.StepInteractionAction.StepInteractionData
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class ActionProcessor(override val scope: Scope) : KoinScopeComponent {

    // lazy initialization injection to avoid circular dependency
    private val appcues: Appcues by inject()
    private val appcuesCoroutineScope: AppcuesCoroutineScope by inject()

    private val actionQueue = Channel<ExperienceAction>(Channel.UNLIMITED)

    init {
        appcuesCoroutineScope.launch {
            for (action in actionQueue) {
                action.execute()
            }
        }
    }

    // This is used for internal actions, like navigation actions processed during step transitions, or
    // post flow completion actions.
    suspend fun process(actions: List<ExperienceAction>) {
        if (actions.isEmpty()) return

        // These actions are executed directly, not placed in the actionQueue Channel.
        // The reason is that they occur inside of the context of another action already executing,
        // typically the Continue action moving to the next step. Since we need to be able to
        // make this a suspend function and wait on them, we cannot place them in the queue or
        // suspend the currently executing action at all, or the queue would become deadlocked.
        transformQueue(actions).toMutableList().forEach { it.execute() }
    }

    // This version is used by the viewModel to process interactive actions from user input - button taps.
    // It includes relevant information about the user interaction that can be used in the auto generated
    // step_interaction event.
    fun process(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        if (actions.isEmpty()) return

        // find step interaction action if there is any
        val stepInteraction = getStepInteractionAction(actions, interactionType, viewDescription)

        appcuesCoroutineScope.launch {
            // the stepInteraction is not included in the transform, and is inserted at the front of queue
            transformQueue(actions).toMutableList()
                .apply { add(0, stepInteraction) }
                // user interactions are processed through the shared queue, to ensure consistency in order of operations
                .forEach { actionQueue.send(it) }
        }
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
        actions: List<ExperienceAction>,
        interactionType: InteractionType,
        viewDescription: String?
    ): StepInteractionAction {
        // reverse the action list to get the first from back to front
        val primaryAction = actions.reversed().firstOrNull { it is MetadataSettingsAction } as? MetadataSettingsAction

        // get an instance of StepInteractionData so it can access
        // StateMachine and AnalyticsTracker
        return scope.get {
            parametersOf(
                StepInteractionData(
                    interactionType = interactionType,
                    viewDescription = viewDescription,
                    category = primaryAction?.category ?: "",
                    destination = primaryAction?.destination ?: "",
                )
            )
        }
    }
}
