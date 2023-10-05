package com.appcues.statemachine.states

import com.appcues.data.model.Experience
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State
import com.appcues.statemachine.Transition
import com.appcues.statemachine.effects.AwaitEffect
import com.appcues.statemachine.effects.ContinuationEffect
import com.appcues.statemachine.effects.PresentationEffect

// AwaitEffect works as a ContinuationSideEffect that AppcuesViewModel will
// send to the state machine once it's done dismissing the current container
// the presence of a non-null value is what tells the UI to dismiss the current container,
// and it should be set to null if a dismiss is not requested (i.e. moving to next step in same container)
internal data class EndingStepState(
    val experience: Experience,
    val flatStepIndex: Int,
    val markComplete: Boolean,
    val awaitEffect: AwaitEffect?,
) : State {

    override val currentExperience: Experience
        get() = experience
    override val currentStepIndex: Int
        get() = flatStepIndex

    override fun take(action: Action): Transition? {
        return when (action) {
            is EndExperience -> toEndingExperience(action)
            is StartStep -> toBeginningStep(action)
            else -> null
        }
    }

    private fun toEndingExperience(action: EndExperience): Transition {
        return next(
            state = EndingExperienceState(experience, flatStepIndex, action.markComplete, action.trackAnalytics),
            sideEffect = ContinuationEffect(Reset)
        )
    }

    private fun toBeginningStep(action: StartStep): Transition {
        return next(
            state = BeginningStepState(experience, action.nextFlatStepIndex),
            sideEffect = PresentationEffect(
                experience = experience,
                flatStepIndex = action.nextFlatStepIndex,
                stepContainerIndex = action.nextStepContainerIndex,
                isDifferentContainer = action.isDifferentContainer
            )
        )
    }
}
