package com.appcues.analytics

import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.ui.ExperienceRenderer
import kotlinx.coroutines.launch

internal class AnalyticsTracker(
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val repository: AppcuesRepository,
    private val experienceRenderer: ExperienceRenderer,
    private val sessionMonitor: SessionMonitor,
    private val activityBuilder: ActivityRequestBuilder,
) {

    fun identify(properties: HashMap<String, Any>? = null) {
        trackActivity(activityBuilder.identify(properties), true)
    }

    fun track(name: String, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        trackActivity(activityBuilder.track(name, properties), sync)
    }

    fun screen(title: String, properties: HashMap<String, Any>? = null) {
        trackActivity(activityBuilder.screen(title, properties), true)
    }

    fun group(properties: HashMap<String, Any>? = null) {
        trackActivity(activityBuilder.group(properties), true)
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
