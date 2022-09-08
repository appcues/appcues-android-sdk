package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.data.model.Experience
import com.appcues.statemachine.Error
import com.appcues.ui.ExperienceStepFormItemState
import com.appcues.ui.ExperienceStepFormItemState.OptionSelectFormItemState
import com.appcues.ui.ExperienceStepFormItemState.TextInputFormItemState

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
        val interactionType: InteractionType,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepInteraction) {
        enum class InteractionType {
            FORM_SUBMITTED
        }
    }

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
        get() = hashMapOf<String, Any?>(
            "experienceId" to experience.id.toString().lowercase(),
            "experienceName" to experience.name,
            "experienceType" to experience.type,
            "version" to experience.publishedAt,
            // items in the spec that we are not ready for yet:
            // "version" to experience.version -- not included in response?
            // "localeName" to "", -- add locale values to analytics for localized experiences
            // "localeId" to "", -- add locale values to analytics for localized experiences
        ).apply {
            flatStepIndex?.let {
                val step = experience.flatSteps[it]
                this["stepId"] = step.id.toString()
                // this is required by SDK debugger to know which step and group is currently showing
                this["stepIndex"] = "${experience.groupLookup[it] ?: 0},${experience.stepIndexLookup[it] ?: 0}"
                this["stepType"] = step.type
            }
            customProperties?.let {
                putAll(it)
            }
            error?.let {
                this["message"] = it.message
                this["errorId"] = it.id.toString()
            }
        }.filterValues { it != null }.mapValues { it.value as Any }

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

    private val customProperties: HashMap<String, Any>?
        get() = when (this) {
            is StepInteraction -> {
                flatStepIndex?.let {
                    val step = experience.flatSteps[it]
                    hashMapOf(
                        "interactionType" to interactionType.analyticsName(),
                        "interactionData" to hashMapOf(
                            "formResponse" to step.formState.formItems.map { formItem ->
                                formItem.analyticsProperties()
                            }
                        ),
                    )
                }
            }
            else -> null
        }

    private fun StepInteraction.InteractionType.analyticsName() =
        when (this) {
            FORM_SUBMITTED -> "Form Submitted"
        }

    private fun ExperienceStepFormItemState.analyticsProperties() =
        hashMapOf<String, Any>(
            "fieldId" to id.toString(),
            "fieldType" to type,
            "fieldRequired" to isRequired,
            "label" to label,
            "value" to formattedValues()
        )

    private fun ExperienceStepFormItemState.formattedValues() =
        when (this) {
            is TextInputFormItemState -> value
            is OptionSelectFormItemState -> values.joinToString(",") // need actual CSV-ifying
        }
}
