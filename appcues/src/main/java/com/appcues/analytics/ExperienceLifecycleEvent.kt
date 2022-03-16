package com.appcues.analytics

import com.appcues.data.model.Experience
import com.appcues.data.model.Step
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.StepError

internal abstract class ExperienceLifecycleEvent(
    val name: String
) {
    abstract val experience: Experience

    data class StepSeen(override val experience: Experience, val stepIndex: Int) :
        ExperienceLifecycleEvent("appcues:v2:step_seen")

    data class StepInteraction(override val experience: Experience, val stepIndex: Int) :
        ExperienceLifecycleEvent("appcues:v2:step_interaction")

    data class StepCompleted(override val experience: Experience, val stepIndex: Int) :
        ExperienceLifecycleEvent("appcues:v2:step_completed")

    data class StepError(val error: com.appcues.statemachine.Error.StepError, override val experience: Experience = error.experience) :
        ExperienceLifecycleEvent("appcues:v2:step_error")

    data class StepRecovered(override val experience: Experience, val stepIndex: Int) :
        ExperienceLifecycleEvent("appcues:v2:step_recovered")

    data class ExperienceStarted(override val experience: Experience) :
        ExperienceLifecycleEvent("appcues:v2:experience_started")

    data class ExperienceCompleted(override val experience: Experience) :
        ExperienceLifecycleEvent("appcues:v2:experience_completed")

    data class ExperienceDismissed(override val experience: Experience, val stepIndex: Int) :
        ExperienceLifecycleEvent("appcues:v2:experience_dismissed")

    data class ExperienceError(
        val error: com.appcues.statemachine.Error.ExperienceError,
        override val experience: Experience = error.experience
    ) : ExperienceLifecycleEvent("appcues:v2:experience_error")

    val properties: HashMap<String, Any>
        get() = hashMapOf<String, Any>(
            "experienceId" to experience.id.toString().lowercase(),
            "experienceName" to experience.name
        ).apply {
            step?.let {
                this["stepId"] = it.id.toString()
            }
            error?.let {
                this["message"] = it.message
                this["errorId"] = it.id.toString()
            }
        }

    private val step: Step?
        get() = when (this) {
            // todo - this needs refactored to handle group/step indexing
            is StepSeen -> experience.stepContainer[this.stepIndex].steps.first()
            is StepInteraction -> experience.stepContainer[this.stepIndex].steps.first()
            is StepCompleted -> experience.stepContainer[this.stepIndex].steps.first()
            is StepError -> experience.stepContainer[this.error.stepIndex].steps.first()
            is StepRecovered -> experience.stepContainer[this.stepIndex].steps.first()
            is ExperienceDismissed -> experience.stepContainer[this.stepIndex].steps.first()
            else -> null
        }

    private val error: Error?
        get() = when (this) {
            is StepError -> this.error
            is ExperienceError -> this.error
            else -> null
        }
}
