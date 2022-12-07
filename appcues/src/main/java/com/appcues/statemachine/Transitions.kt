@file:Suppress("unused", "UNUSED_PARAMETER")

package com.appcues.statemachine

import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action.Trigger.NAVIGATE
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.SideEffect.AwaitEffect
import com.appcues.statemachine.SideEffect.ContinuationEffect
import com.appcues.statemachine.SideEffect.PresentContainerEffect
import com.appcues.statemachine.SideEffect.ProcessActions
import com.appcues.statemachine.SideEffect.ReportErrorEffect
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.util.ResultOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal interface Transitions {

    fun Idling.fromIdlingToBeginningExperience(action: StartExperience): Transition {
        return if (!action.experience.error.isNullOrEmpty()) {
            Transition(null, ReportErrorEffect(ExperienceError(action.experience, action.experience.error)))
        } else if (action.experience.stepContainers.isEmpty()) {
            Transition(null, ReportErrorEffect(ExperienceError(action.experience, "Experience has 0 steps")))
        } else {
            Transition(BeginningExperience(action.experience), ContinuationEffect(StartStep(StepIndex(0))))
        }
    }

    fun BeginningExperience.fromBeginningExperienceToBeginningStep(
        action: StartStep,
        coroutineScope: CoroutineScope,
        continuation: suspend () -> ResultOf<State, Error>
    ): Transition {
        val completion: CompletableDeferred<ResultOf<State, Error>> = CompletableDeferred()

        // This is a safeguard against trying to load a step container that has zero steps.
        // We already guard against loading an experience with zero steps (groups) in the BeginningExperience
        // transition above. However, if a group exists, but has zero steps within - it will get here and
        // fail, so that we don't launch an AppcuesActivity and then have no content to render
        experience.stepContainers.firstOrNull()?.let {
            if (it.steps.isEmpty()) {
                return stepNotFoundErrorTransition(experience, 0, 0)
            }
        }

        // for pre-step navigation actions - only allow these to execute if this experience is being launched for some
        // other reason that Qualification (i.e. deep links, preview, manual show). For any qualified experience, the initial
        // starting state of the experience is determined solely by flow settings determining the trigger
        // (i.e. trigger on certain screen).
        val actions = if (experience.trigger is Qualification) emptyList() else experience.getNavigationActions(0)

        return Transition(
            state = BeginningStep(experience, 0, true) {
                coroutineScope.launch {
                    completion.complete(continuation())
                }
            },
            sideEffect = PresentContainerEffect(experience, 0, completion, actions)
        )
    }

    fun BeginningStep.fromBeginningStepToRenderingStep(action: RenderStep): Transition {
        return Transition(RenderingStep(experience, flatStepIndex, isFirst))
    }

    fun RenderingStep.fromRenderingStepToEndingStep(
        action: StartStep,
        coroutineScope: CoroutineScope,
        continuation: suspend () -> ResultOf<State, Error>
    ): Transition {
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            if (isValidStepIndex(nextStepIndex, experience)) {
                if (experience.areStepsFromDifferentGroup(flatStepIndex, nextStepIndex)) {
                    // in different groups we want to wait for StartStep action from AppcuesViewModel
                    val response = CompletableDeferred<ResultOf<State, Error>>()
                    Transition(
                        state = EndingStep(experience, flatStepIndex, nextStepIndex > flatStepIndex) {
                            coroutineScope.launch {
                                response.complete(continuation())
                            }
                        },
                        sideEffect = AwaitEffect(response)
                    )
                } else {
                    // in same group we can continue to StartStep internally
                    Transition(
                        state = EndingStep(experience, flatStepIndex, nextStepIndex > flatStepIndex, null),
                        sideEffect = ContinuationEffect(StartStep(action.stepReference)),
                    )
                }
            } else {
                // next step index is out of bounds error
                errorTransition(experience, flatStepIndex, "Step at ${action.stepReference} does not exist")
            }
        }
    }

    fun RenderingStep.fromRenderingStepToEndingExperience(
        action: EndExperience,
        coroutineScope: CoroutineScope,
        continuation: suspend () -> ResultOf<State, Error>
    ): Transition {
        return if (action.destroyed) {
            // this means the AppcuesActivity was destroyed externally (i.e. deep link) and we should
            // immediately transition to EndingExperience - not rely on the UI to do it for us (it's gone)
            Transition(
                state = EndingStep(experience, flatStepIndex, action.markComplete, null),
                sideEffect = ContinuationEffect(action)
            )
        } else {
            // otherwise, its a natural end of experience from an in-experience action / dismiss
            // and should be communicated to the UI layer to dismiss itself.
            //
            // instead of using sideEffect we pass EndExperience on EndingStep
            // then AppcuesViewModel will continue to EndExperience when appropriate
            val response = CompletableDeferred<ResultOf<State, Error>>()
            Transition(
                state = EndingStep(experience, flatStepIndex, action.markComplete) {
                    coroutineScope.launch {
                        response.complete(continuation())
                    }
                },
                sideEffect = AwaitEffect(response)
            )
        }
    }

    fun EndingStep.fromEndingStepToEndingExperience(action: EndExperience): Transition {
        return Transition(EndingExperience(experience, flatStepIndex, action.markComplete), ContinuationEffect(Reset))
    }

    fun EndingStep.fromEndingStepToBeginningStep(
        action: StartStep,
        coroutineScope: CoroutineScope,
        continuation: suspend () -> ResultOf<State, Error>
    ): Transition {
        // get next step index
        return action.stepReference.getIndex(experience, flatStepIndex).let { nextStepIndex ->
            // check if next step index is valid for this experience
            if (isValidStepIndex(nextStepIndex, experience)) {
                // check if current step and next step are from different step container
                transitionsToBeginningStep(coroutineScope, experience, flatStepIndex, nextStepIndex, continuation)
            } else {
                // next step index is out of bounds error
                errorTransition(experience, flatStepIndex, "Step at ${action.stepReference} does not exist")
            }
        }
    }

    fun EndingExperience.fromEndingExperienceToIdling(action: Reset): Transition {
        return Transition(Idling, if (markComplete) ProcessActions(experience.completionActions) else null)
    }

    companion object : Transitions
}

@OptIn(ExperimentalContracts::class)
private fun isValidStepIndex(stepIndex: Int?, experience: Experience): Boolean {
    contract {
        returns(true) implies (stepIndex != null)
    }

    return stepIndex != null && stepIndex >= 0 && stepIndex < experience.flatSteps.size
}

private fun transitionsToBeginningStep(
    coroutineScope: CoroutineScope,
    experience: Experience,
    currentStepIndex: Int,
    nextStepIndex: Int,
    continuation: suspend () -> ResultOf<State, Error>,
) = if (experience.areStepsFromDifferentGroup(currentStepIndex, nextStepIndex)) {
    // given that steps are from different container, we now get step container index to present
    experience.groupLookup[nextStepIndex]?.let { stepContainerIndex ->
        val actions = experience.getNavigationActions(stepContainerIndex)
        val completion: CompletableDeferred<ResultOf<State, Error>> = CompletableDeferred()
        Transition(
            state = BeginningStep(experience, nextStepIndex, false) {
                coroutineScope.launch {
                    completion.complete(continuation())
                }
            },
            sideEffect = PresentContainerEffect(experience, stepContainerIndex, completion, actions)
        )
    } ?: run {
        // this should never happen at this point. but better to safe guard anyways
        errorTransition(experience, currentStepIndex, "StepContainer for nextStepIndex $nextStepIndex not found")
    }
} else {
    // else we just transition to BeginningStep, and the UI will invoke the continuation
    // once render is complete
    val completion: CompletableDeferred<ResultOf<State, Error>> = CompletableDeferred()
    Transition(
        state = BeginningStep(experience, nextStepIndex, false) {
            coroutineScope.launch {
                completion.complete(continuation())
            }
        },
        sideEffect = AwaitEffect(completion)
    )
}

// Gets any actions defined on the step group container for the "navigate" trigger. These are the
// actions that should be executed before presenting this group's container.
private fun Experience.getNavigationActions(stepContainerIndex: Int): List<ExperienceAction> {
    val stepGroup = stepContainers[stepContainerIndex]
    return stepGroup.actions[stepGroup.id]?.filter { it.on == NAVIGATE }?.map { it.experienceAction } ?: emptyList()
}

private fun Experience.areStepsFromDifferentGroup(stepIndexOne: Int, stepIndexTwo: Int): Boolean {
    return groupLookup[stepIndexOne] != groupLookup[stepIndexTwo]
}

private fun errorTransition(experience: Experience, currentStepIndex: Int, message: String): Transition {
    return Transition(
        state = null,
        sideEffect = ReportErrorEffect(StepError(experience, currentStepIndex, message))
    )
}

private fun stepNotFoundErrorTransition(experience: Experience, groupIndex: Int, stepIndex: Int): Transition {
    return Transition(
        state = Idling,
        sideEffect = ReportErrorEffect(
            error = StepError(experience, 0, "step group $groupIndex doesn't contain a child step at index $stepIndex")
        )
    )
}
