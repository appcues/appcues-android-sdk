package com.appcues.analytics

import com.appcues.analytics.RenderingService.EventTracker
import com.appcues.data.model.Experience
import com.appcues.data.model.Experiment
import com.appcues.util.appcuesFormatted
import java.util.UUID

internal fun EventTracker.track(experiment: Experiment) {
    trackEvent(
        AnalyticsEvent.ExperimentEntered.eventName,
        mapOf(
            "experimentId" to experiment.id.appcuesFormatted(),
            "experimentGroup" to experiment.group,
            "experimentExperienceId" to experiment.experienceId.appcuesFormatted(),
            "experimentGoalId" to experiment.goalId,
            "experimentContentType" to experiment.contentType,
        ),
        isInteractive = false,
        isInternal = true,
    )
}

internal fun EventTracker.trackExperienceError(experience: Experience, message: String) {
    val errorId = UUID.randomUUID()

    trackEvent(
        AnalyticsEvent.ExperienceError.eventName,
        mapOf(
            "message" to message,
            "errorId" to errorId.toString(),
        ),
        isInteractive = false,
        isInternal = true,
    )

    experience.renderErrorId = errorId
}

internal fun EventTracker.trackExperienceRecovery(experience: Experience) {
    if (experience.renderErrorId == null) return

    val errorId = experience.renderErrorId

    trackEvent(
        AnalyticsEvent.ExperienceRecovery.eventName,
        mapOf(
            "errorId" to errorId.toString(),
        ),
        isInteractive = false,
        isInternal = true,
    )

    experience.renderErrorId = null
}
