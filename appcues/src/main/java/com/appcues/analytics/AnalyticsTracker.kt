package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.ui.ExperienceRenderer

internal class AnalyticsTracker(
    private val config: AppcuesConfig,
    private val repository: AppcuesRepository,
    private val storage: Storage,
    private val experienceRenderer: ExperienceRenderer
) {
    suspend fun identify(properties: HashMap<String, Any>? = null) {
        val activity = ActivityRequest(
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId,
            profileUpdate = properties
        )
        trackActivity(activity, true)
    }

    suspend fun track(name: String, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        val activity = ActivityRequest(
            events = listOf(EventRequest(name = name, attributes = properties)),
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId
        )
        trackActivity(activity, sync)
    }

    suspend fun screen(title: String, properties: HashMap<String, Any>? = null) {
        // screen calls are really just a special type of event: "appcues:screen_view"
        val updatedProperties = properties ?: hashMapOf()
        // include the "screenTitle" property automatically
        updatedProperties["screenTitle"] = title
        // handle the same as other events
        track("appcues:screen_view", updatedProperties, true)
    }

    suspend fun group(properties: HashMap<String, Any>? = null) {
        val activity = ActivityRequest(
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId,
            groupUpdate = properties
        )
        trackActivity(activity, true)
    }

    private suspend fun trackActivity(activity: ActivityRequest, sync: Boolean) {

        // todo - will need to revisit with proper session handling, but this means no user identified
        if (storage.userId.isEmpty()) return

        // this will respond with qualified experiences, if applicable
        val experiences = repository.trackActivity(activity, sync)

        if (sync && experiences.isNotEmpty()) {
            // note: by default we just show the first experience, but will need to revisit and allow
            // for showing secondary qualified experience if the first fails to load for some reason
            experienceRenderer.show(experiences.first())
        }
    }
}
