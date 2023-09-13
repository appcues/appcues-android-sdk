package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_TAPPED
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.model.getFrameId
import com.appcues.statemachine.Error
import com.appcues.util.appcuesFormatted
import com.appcues.util.toSlug

internal sealed class ExperienceLifecycleEvent(
    open val experience: Experience,
    val event: AnalyticsEvent
) {

    companion object {

        const val INTERACTION_TYPE_KEY = "interactionType"
        const val INTERACTION_DATA_KEY = "interactionData"
        const val FORM_SUBMITTED_INTERACTION_TYPE = "Form Submitted"
        const val BUTTON_TAPPED_INTERACTION_TYPE = "Button Tapped"
        const val BUTTON_LONG_PRESSED_INTERACTION_TYPE = "Button Long Pressed"
        const val TARGET_TAPPED_INTERACTION_TYPE = "Target Tapped"
        const val TARGET_LONG_PRESSED_INTERACTION_TYPE = "Target Long Pressed"
    }

    data class StepSeen(
        override val experience: Experience,
        val stepIndex: Int,
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepSeen)

    data class StepInteraction(
        override val experience: Experience,
        val stepIndex: Int,
        val interactionType: InteractionType,
        val interactionProperties: HashMap<String, Any> = hashMapOf()
    ) : ExperienceLifecycleEvent(experience, AnalyticsEvent.ExperienceStepInteraction) {

        enum class InteractionType {
            FORM_SUBMITTED, BUTTON_TAPPED, BUTTON_LONG_PRESSED, TARGET_TAPPED, TARGET_LONG_PRESSED
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
            "experienceId" to experience.id.appcuesFormatted(),
            "experienceInstanceId" to experience.instanceId.appcuesFormatted(),
            "experienceName" to experience.name,
            "experienceType" to experience.type,
            "frameId" to experience.renderContext.getFrameId(),
            "version" to experience.publishedAt,
            // items in the spec that we are not ready for yet:
            // "version" to experience.version -- not included in response?
            // "localeName" to "", -- add locale values to analytics for localized experiences
            // "localeId" to "", -- add locale values to analytics for localized experiences
        ).apply {
            flatStepIndex?.let {
                experience.flatSteps.getOrNull(it)?.let { step ->
                    this["stepId"] = step.id.appcuesFormatted()
                    // this is required by SDK debugger to know which step and group is currently showing
                    this["stepIndex"] = "${experience.groupLookup[it] ?: 0},${experience.stepIndexLookup[it] ?: 0}"
                    this["stepType"] = step.type
                }
            }
            customProperties?.let {
                putAll(it)
            }
            error?.let {
                this["message"] = it.message
                this["errorId"] = it.id.appcuesFormatted()
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
                        INTERACTION_TYPE_KEY to interactionType.analyticsName(),
                        INTERACTION_DATA_KEY to when (interactionType) {
                            FORM_SUBMITTED -> step.formState
                            BUTTON_TAPPED -> interactionProperties
                            BUTTON_LONG_PRESSED -> interactionProperties
                            TARGET_TAPPED -> interactionProperties
                            TARGET_LONG_PRESSED -> interactionProperties
                        },
                    )
                }
            }
            else -> null
        }

    private fun StepInteraction.InteractionType.analyticsName() =
        when (this) {
            FORM_SUBMITTED -> FORM_SUBMITTED_INTERACTION_TYPE
            BUTTON_TAPPED -> BUTTON_TAPPED_INTERACTION_TYPE
            BUTTON_LONG_PRESSED -> BUTTON_LONG_PRESSED_INTERACTION_TYPE
            TARGET_TAPPED -> TARGET_TAPPED_INTERACTION_TYPE
            TARGET_LONG_PRESSED -> TARGET_LONG_PRESSED_INTERACTION_TYPE
        }
}

internal fun ExperienceStepFormState.formattedAsProfileUpdate(): Map<String, Any> {
    val profileUpdate = hashMapOf<String, Any>()

    formItems.forEach {
        profileUpdate["_appcuesForm_${it.label.toSlug()}"] = it.value

        if (it.attributeName != null) {
            profileUpdate[it.attributeName] = it.value
        }
    }

    return profileUpdate
}
