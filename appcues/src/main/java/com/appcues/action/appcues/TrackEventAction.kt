package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigOrDefault

internal class TrackEventAction(
    override val config: AppcuesConfigMap,
    private val analyticsTracker: AnalyticsTracker,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/track"

        private fun buildConfigMap(eventName: String, attributes: Map<String, Any>?): AppcuesConfigMap {
            return mutableMapOf<String, Any>().apply {
                put("eventName", eventName)
                attributes?.let { put("attributes", it) }
            }
        }
    }

    constructor(
        analyticsTracker: AnalyticsTracker,
        eventName: String,
        attributes: Map<String, Any>?
    ) : this(buildConfigMap(eventName, attributes), analyticsTracker)

    private val eventName = config.getConfigOrDefault<String?>("eventName", null)

    private val attributes = config.getConfig<Map<String, Any>?>("attributes")

    override suspend fun execute() {
        if (!eventName.isNullOrEmpty()) {
            analyticsTracker.track(eventName, attributes)
        }
    }
}
