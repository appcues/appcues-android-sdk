package com.appcues.statemachine

import com.appcues.data.model.Experience

internal sealed class State {

    object Idling : State()
    data class BeginningExperience(val experience: Experience) : State()
    data class BeginningStep(
        val experience: Experience,
        val flatStepIndex: Int,
        val isFirst: Boolean,
        // this is how the UI communicates success/failure in presentation
        // back to the state machine
        val presentationComplete: (() -> Unit),
    ) : State()

    data class RenderingStep(val experience: Experience, val flatStepIndex: Int, val isFirst: Boolean) : State()
    data class EndingStep(
        val experience: Experience,
        val flatStepIndex: Int,
        val markComplete: Boolean,
        // this works as a ContinuationSideEffect that AppcuesViewModel will
        // send to the state machine once it's done dismissing the current container
        // the presence of a non-null value is what tells the UI to dismiss the current container,
        // and it should be set to null if a dismiss is not requested (i.e. moving to next step in same container)
        val dismissAndContinue: (() -> Unit)?,
    ) : State() {
        val isStepCompleted
            get() = markComplete || flatStepIndex == experience.flatSteps.count() - 1
    }

    data class EndingExperience(val experience: Experience, val flatStepIndex: Int, val markComplete: Boolean) : State() {
        // this defines whether the experience was completed or dismissed
        val isExperienceCompleted
            get() = markComplete || flatStepIndex == experience.flatSteps.count() - 1
    }

    data class Paused(val state: State) : State()

    val currentExperience: Experience?
        get() = when (this) {
            is Idling -> null
            is BeginningExperience -> this.experience
            is BeginningStep -> this.experience
            is EndingExperience -> this.experience
            is EndingStep -> this.experience
            is Paused -> this.state.currentExperience
            is RenderingStep -> this.experience
        }

    val currentStepIndex: Int?
        get() = when (this) {
            is Idling -> null
            is BeginningExperience -> null
            is BeginningStep -> this.flatStepIndex
            is EndingExperience -> this.flatStepIndex
            is EndingStep -> this.flatStepIndex
            is Paused -> this.state.currentStepIndex
            is RenderingStep -> this.flatStepIndex
        }
}
