package com.appcues.data.model

import java.util.UUID

internal sealed class ExperienceTrigger {
    data class Qualification(val reason: String?) : ExperienceTrigger()
    data class ExperienceCompletionAction(
        val fromExperienceId: UUID,
        val campaignId: String? = null,
        val tacticId: String? = null,
    ) : ExperienceTrigger()
    data class LaunchExperienceAction(
        val fromExperienceId: UUID?,
        val campaignId: String? = null,
        val tacticId: String? = null,
    ) : ExperienceTrigger()
    data class PushNotification(val fromPushId: UUID) : ExperienceTrigger()
    object ShowCall : ExperienceTrigger()
    object DeepLink : ExperienceTrigger()
    object Preview : ExperienceTrigger()
}
