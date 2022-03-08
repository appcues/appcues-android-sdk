package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
    private val repository: AppcuesRepository,
    private val storage: Storage,
    private val experienceRenderer: ExperienceRenderer,
    private val sessionMonitor: SessionMonitor
) {

    fun identify(properties: HashMap<String, Any>? = null) {
        val activity = ActivityRequest(
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId,
            profileUpdate = properties
        )
        trackActivity(activity, true)
    }

    fun track(name: String, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        val activity = ActivityRequest(
            events = listOf(EventRequest(name = name, attributes = properties)),
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId
        )
        trackActivity(activity, sync)
    }

    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        // screen calls are really just a special type of event: "appcues:screen_view"
        val updatedProperties = properties ?: hashMapOf()
        // include the "screenTitle" property automatically
        updatedProperties["screenTitle"] = title
        // handle the same as other events
        track("appcues:screen_view", updatedProperties, true)
    }

    fun group(properties: HashMap<String, Any>? = null) {
        val activity = ActivityRequest(
            userId = storage.userId,
            groupId = storage.groupId,
            accountId = config.accountId,
            groupUpdate = properties
        )
        trackActivity(activity, true)
    }

    private fun trackActivity(activity: ActivityRequest, sync: Boolean) {

        if (!sessionMonitor.isActive) return

        appcuesCoroutineScope.launch {
            // this will respond with qualified experiences, if applicable
            val experiences = repository.trackActivity(activity, sync)

            if (sync && experiences.isNotEmpty()) {
                // note: by default we just show the first experience, but will need to revisit and allow
                // for showing secondary qualified experience if the first fails to load for some reason
                experienceRenderer.show(experiences.first())
            }
        }
    }
}
