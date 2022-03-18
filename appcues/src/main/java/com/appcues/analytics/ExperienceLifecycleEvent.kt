package com.appcues.analytics

import com.appcues.data.model.Experience
import com.appcues.data.model.Step
import com.appcues.statemachine.Error

internal sealed class ExperienceLifecycleEvent(
    val name: String
) {
    abstract val experience: Experience

    data class StepSeen(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent("appcues:v2:step_seen")

    data class StepInteraction(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent("appcues:v2:step_interaction")

    data class StepCompleted(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent("appcues:v2:step_completed")

    data class StepError(
        val stepError: Error.StepError,
        override val experience: Experience = stepError.experience,
    ) : ExperienceLifecycleEvent("appcues:v2:step_error")

    data class StepRecovered(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent("appcues:v2:step_recovered")

    data class ExperienceStarted(
        override val experience: Experience,
    ) : ExperienceLifecycleEvent("appcues:v2:experience_started")

    data class ExperienceCompleted(
        override val experience: Experience,
    ) : ExperienceLifecycleEvent("appcues:v2:experience_completed")

    data class ExperienceDismissed(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent("appcues:v2:experience_dismissed")

    data class ExperienceError(
        val experienceError: Error.ExperienceError,
        override val experience: Experience = experienceError.experience
    ) : ExperienceLifecycleEvent("appcues:v2:experience_error")

    val properties: HashMap<String, Any>
        get() = hashMapOf<String, Any>(
            "experienceId" to experience.id.toString().lowercase(),
            "experienceName" to experience.name,
            // items in the spec that we are not ready for yet:
            // "version" to experience.version -- not included in response?
            // "localeName" to "", -- add locale values to analytics for localized experiences
            // "localeId" to "", -- add locale values to analytics for localized experiences
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
            is StepError -> experience.stepContainer[this.stepError.stepIndex].steps.first()
            is StepRecovered -> experience.stepContainer[this.stepIndex].steps.first()
            is ExperienceDismissed -> experience.stepContainer[this.stepIndex].steps.first()
            else -> null
        }

    private val error: Error?
        get() = when (this) {
            is StepError -> stepError
            is ExperienceError -> experienceError
            else -> null
        }
}
