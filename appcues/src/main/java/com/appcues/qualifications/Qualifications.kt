package com.appcues.qualifications

import com.appcues.analytics.AnalyticsEvent
import com.appcues.data.AppcuesRepository
import com.appcues.data.local.RulesLocalSource
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.LocalQualification
import com.appcues.data.model.QualificationResult
import com.appcues.data.model.rules.ExperienceRules
import com.appcues.data.model.rules.RuleFrequency.ONCE
import com.appcues.data.remote.appcues.request.ActivityRequest
import com.appcues.data.remote.appcues.request.EventRequest
import com.appcues.data.remote.sdksettings.AppcuesBundleRemoteSource
import java.util.UUID

internal class Qualifications(
    private val repository: AppcuesRepository,
    private val bundleRemote: AppcuesBundleRemoteSource,
    private val rulesLocal: RulesLocalSource,
) {

    var localQualification: LocalQualification? = null
    suspend fun qualify(activity: ActivityRequest): QualificationResult? {
        // on session Started, refresh offline qualifications
        activity.events.onEvent(AnalyticsEvent.SessionStarted) {
            localQualification = bundleRemote.getLocalQualification(activity.accountId)
        }

        // on experience started, insert it to register in local storage
        activity.events.onEvent(AnalyticsEvent.ExperienceStarted) {
            rulesLocal.insert(
                ExperienceRules(
                    experienceId = UUID.fromString(it.attributes["experienceId"] as String),
                    userId = activity.userId,
                    seenAt = it.timestamp
                )
            )
        }

        // run offline qualification
        val offlineQualification = checkLocalQualification(activity)
        return if (offlineQualification.isNotEmpty()) {
            repository.trackActivity(activity, qualify = false)

            QualificationResult(Qualification("offline"), offlineQualification)
        } else {
            // qualify online
            repository.trackActivity(activity)
        }
    }

    private suspend fun Iterable<EventRequest>?.onEvent(analyticsEvent: AnalyticsEvent, onBlock: suspend (EventRequest) -> Unit) {
        if (this == null) return

        filter { it.name == analyticsEvent.eventName }.onEach { onBlock(it) }
    }

    private suspend fun checkLocalQualification(activity: ActivityRequest): List<Experience> {
        val runningLocalQualification = localQualification

        if (activity.events == null || runningLocalQualification == null) return emptyList()

        return runningLocalQualification.qualifications
            .filter {
                // basic check for frequency, when frequency is ONCE and its already recorded then we filter out before evaluation
                if (it.rule.frequency == ONCE && rulesLocal.getViewCount(activity.userId, it.experience.id) > 0) return@filter false
                // keep experience in list given that at least one event evaluates true (qualified)
                activity.events.any { request -> it.rule.evaluate(request, activity.profileUpdate?.toMap()) }
            }
            .sortedByDescending { it.sortingPriority }
            .map { it.experience }
    }
}
