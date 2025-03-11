package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_LONG_PRESSED
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.TARGET_TAPPED
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.DeepLink
import com.appcues.data.model.ExperienceTrigger.ExperienceCompletionAction
import com.appcues.data.model.ExperienceTrigger.LaunchExperienceAction
import com.appcues.data.model.ExperienceTrigger.Preview
import com.appcues.data.model.ExperienceTrigger.PushNotification
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.ExperienceTrigger.ShowCall
import com.appcues.data.model.getFrameId
import com.appcues.statemachine.Error
import com.appcues.util.appcuesFormatted

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
        get() = hashMapOf<String, Any?>()
            .experienceProperties()
            .stepProperties()
            .eventProperties()
            // filter null props and map from Map<String, Any?> to Map<String, Any>
            .filterValues { it != null }.mapValues { it.value as Any }

    private val triggerValue: String?
        get() = when (val trigger = experience.trigger) {
            DeepLink -> "deep_link"
            is ExperienceCompletionAction -> "experience_completion_action"
            is LaunchExperienceAction -> "launch_action"
            Preview -> "preview"
            is PushNotification -> "push_notification"
            is Qualification -> trigger.reason
            ShowCall -> "show_call"
        }

    private fun HashMap<String, Any?>.experienceProperties() = apply {
        this["experienceId"] = experience.id.appcuesFormatted()
        this["experienceInstanceId"] = experience.instanceId.appcuesFormatted()
        this["experienceName"] = experience.name
        this["experienceType"] = experience.type
        this["frameId"] = experience.renderContext.getFrameId()
        this["version"] = experience.publishedAt
        this["localeName"] = experience.localeName
        this["localeId"] = experience.localeId
        this["workflowId"] = experience.workflowId?.appcuesFormatted()
        this["workflowTaskId"] = experience.workflowTaskId?.appcuesFormatted()
        this["trigger"] = triggerValue

        when (val trigger = experience.trigger) {
            is ExperienceCompletionAction -> {
                this["fromExperienceId"] = trigger.fromExperienceId.appcuesFormatted()
            }
            is LaunchExperienceAction -> {
                this["fromExperienceId"] = trigger.fromExperienceId?.appcuesFormatted()
            }
            is PushNotification -> {
                this["pushNotificationId"] = trigger.fromPushId.appcuesFormatted()
            }
            else -> Unit
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

    private fun HashMap<String, Any?>.stepProperties() = apply {
        flatStepIndex?.let {
            experience.flatSteps.getOrNull(it)?.let { step ->
                this["stepId"] = step.id.appcuesFormatted()
                // this is required by SDK debugger to know which step and group is currently showing
                this["stepIndex"] = "${experience.groupLookup[it] ?: 0},${experience.stepIndexLookup[it] ?: 0}"
                this["stepType"] = step.type
            }
        }
    }

    private fun HashMap<String, Any?>.eventProperties() = apply {
        when (this@ExperienceLifecycleEvent) {
            is StepInteraction -> {
                val step = experience.flatSteps[stepIndex]

                this[INTERACTION_TYPE_KEY] = interactionType.analyticsName()
                this[INTERACTION_DATA_KEY] = when (interactionType) {
                    FORM_SUBMITTED -> step.formState
                    BUTTON_TAPPED -> interactionProperties
                    BUTTON_LONG_PRESSED -> interactionProperties
                    TARGET_TAPPED -> interactionProperties
                    TARGET_LONG_PRESSED -> interactionProperties
                }
            }
            is StepError -> {
                this["message"] = stepError.message
                this["errorId"] = stepError.id.appcuesFormatted()
            }
            is ExperienceError -> {
                this["message"] = experienceError.message
                this["errorId"] = experienceError.id.appcuesFormatted()
            }
            else -> Unit
        }
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
