package com.appcues.analytics

import com.appcues.data.model.Experiment
import com.appcues.util.appcuesFormatted

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
