package com.appcues.analytics

import com.appcues.analytics.ExperienceLifecycleEvent.ExperienceError
import com.appcues.model.Experience
import com.appcues.model.Experiment
import com.appcues.statemachine.Error
import com.appcues.util.appcuesFormatted
import java.util.UUID

internal fun AnalyticsTracker.track(event: AnalyticsEvent, properties: Map<String, Any>? = null, interactive: Boolean = true) {
    track(event.eventName, properties, interactive, true)
}

internal fun AnalyticsTracker.track(experiment: Experiment) {
    track(
        event = AnalyticsEvent.ExperimentEntered,
        properties = mapOf(
            "experimentId" to experiment.id.appcuesFormatted(),
            "experimentGroup" to experiment.group,
            "experimentExperienceId" to experiment.experienceId.appcuesFormatted(),
            "experimentGoalId" to experiment.goalId,
            "experimentContentType" to experiment.contentType,
        ),
        interactive = false
    )
}

internal fun AnalyticsTracker.trackRecoverableExperienceError(experience: Experience, message: String) {
    // only track the recoverable error once
    if (experience.renderErrorId != null) return

    experience.renderErrorId = UUID.randomUUID()
    val error = ExperienceError(Error.ExperienceError(experience, message, experience.renderErrorId))

    track(
        name = error.name,
        properties = error.properties,
        interactive = false,
        isInternal = true,
    )
}

internal fun AnalyticsTracker.trackExperienceRecovery(experience: Experience) {
    if (experience.renderErrorId == null) return

    val errorId = experience.renderErrorId

    track(
        event = AnalyticsEvent.ExperienceRecovery,
        properties = mapOf(
            "errorId" to errorId.toString(),
        ),
        interactive = false
    )

    experience.renderErrorId = null
}
