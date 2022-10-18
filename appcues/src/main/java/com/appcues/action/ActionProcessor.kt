package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.action.appcues.StepInteractionAction.StepInteractionData
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class ActionProcessor(
    override val scope: Scope,
    private val actionQueue: ActionQueue
) : KoinScopeComponent {

    // lazy initialization injection to avoid circular dependency
    private val appcues: Appcues by inject()
    private val appcuesCoroutineScope: AppcuesCoroutineScope by inject()

    init {
        appcuesCoroutineScope.launch {
            for (action in actionQueue.queue) {
                action.execute(appcues)
            }
        }
    }

    fun process(actions: List<ExperienceAction>) {
        transformQueue(actions).send()
    }

    // second process with extra parameters is used by the viewModel to communicate
    // extra relevant information regarding the user interaction that let to process
    // all the actions.
    fun process(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        // find step interaction action if there is any
        val stepInteraction = arrayListOf<ExperienceAction>().apply {
            getStepInteractionAction(actions, interactionType, viewDescription)?.also { add(it) }
        }
        // send it before processing all existing actions
        stepInteraction.send()
        // process actions
        process(actions)
    }

    private fun List<ExperienceAction>.send() {
        actionQueue.enqueue(this)
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
    ): StepInteractionAction? {
        // reverse the action list to get the first from back to front
        return actions.reversed()
            .firstOrNull { it is MetadataSettingsAction }
            ?.let { action ->
                with(action as MetadataSettingsAction) {
                    // get an instance of StepInteractionData so it can access
                    // StateMachine and AnalyticsTracker
                    scope.get {
                        parametersOf(
                            StepInteractionData(
                                interactionType = interactionType,
                                viewDescription = viewDescription,
                                category = category,
                                destination = destination,
                            )
                        )
                    }
                }
            }
    }
}
