package com.appcues.data.model

import java.util.UUID

internal sealed class ExperienceTrigger {
    data class Qualification(val reason: String?) : ExperienceTrigger() {
        companion object {

            const val REASON_SCREEN_VIEW = "screen_view"
        }
    }

    data class ExperienceCompletionAction(val fromExperienceId: UUID?) : ExperienceTrigger()

    // val fromExperienceId: UUID was not being used
    object LaunchExperienceAction : ExperienceTrigger()
    object ShowCall : ExperienceTrigger()
    object DeepLink : ExperienceTrigger()
    object Preview : ExperienceTrigger()
}
