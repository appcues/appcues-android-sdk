package com.appcues.analytics

import com.appcues.data.model.Experience
import com.appcues.data.model.Experiment
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

internal fun AnalyticsTracker.trackExperienceError(experience: Experience, message: String) {
    val errorId = UUID.randomUUID()

    track(
        event = AnalyticsEvent.ExperienceError,
        properties = mapOf(
            "message" to message,
            "errorId" to errorId.toString(),
        ),
        interactive = false
    )

    experience.renderErrorId = errorId
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
