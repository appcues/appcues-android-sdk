package com.appcues.data.model

import java.util.UUID

internal sealed class ExperienceTrigger {
    data class Qualification(val reason: String?) : ExperienceTrigger()
    data class ExperienceCompletionAction(val fromExperienceId: UUID?) : ExperienceTrigger()
    data class LaunchExperienceAction(val fromExperienceId: UUID?) : ExperienceTrigger()
    object ShowCall : ExperienceTrigger()
    object DeepLink : ExperienceTrigger()
    data class Notification(val notificationId: String): ExperienceTrigger()
    object Preview : ExperienceTrigger()
}
