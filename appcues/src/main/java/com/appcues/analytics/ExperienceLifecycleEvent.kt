package com.appcues.analytics

import com.appcues.data.model.Experience
import com.appcues.statemachine.Error

internal sealed class ExperienceLifecycleEvent(
    open val experience: Experience,
    val event: AnalyticsEvent
) {

    data class StepSeen(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepSeen)

    data class StepInteraction(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepInteraction)

    data class StepCompleted(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepCompleted)

    data class StepError(
        val stepError: Error.StepError,
        override val experience: Experience = stepError.experience,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepError)

    data class StepRecovered(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepRecovered)

    data class ExperienceStarted(
        override val experience: Experience,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStarted)

    data class ExperienceCompleted(
        override val experience: Experience,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceCompleted)

    data class ExperienceDismissed(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceDismissed)

    data class ExperienceError(
        val experienceError: Error.ExperienceError,
        override val experience: Experience = experienceError.experience
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceError)

    val name: String
        get() = event.eventName

    val properties: Map<String, Any>
        get() = hashMapOf<String, Any>(
            "experienceId" to experience.id.toString().lowercase(),
            "experienceName" to experience.name,
            // items in the spec that we are not ready for yet:
            // "version" to experience.version -- not included in response?
            // "localeName" to "", -- add locale values to analytics for localized experiences
            // "localeId" to "", -- add locale values to analytics for localized experiences
        ).apply {
            flatStepIndex?.let {
                this["stepId"] = experience.flatSteps[it].id.toString()
                // this is required by SDK debugger to know which step and group is currently showing
                this["stepIndex"] = "${experience.groupLookup[it] ?: 0},${experience.stepIndexLookup[it] ?: 0}"
            }
            error?.let {
                this["message"] = it.message
                this["errorId"] = it.id.toString()
            }
        }

    private val flatStepIndex: Int?
        get() = when (this) {
            is StepSeen -> stepIndex
            is StepInteraction -> stepIndex
            is StepCompleted -> stepIndex
            is StepError -> stepError.stepIndex
            is StepRecovered -> stepIndex
            is ExperienceDismissed -> stepIndex
            else -> null
        }

    private val error: Error?
        get() = when (this) {
            is StepError -> stepError
            is ExperienceError -> experienceError
            else -> null
        }
}
