package com.appcues.analytics

import com.appcues.AppcuesConfig
import com.appcues.AppcuesSession
import com.appcues.data.AppcuesRepository
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.request.EventRequest
import com.appcues.ui.ExperienceRenderer

internal class AnalyticsTracker(
    private val config: AppcuesConfig,
    private val repository: AppcuesRepository,
    private val session: AppcuesSession,
    private val experienceRenderer: ExperienceRenderer
) {

    suspend fun track(name: String, properties: HashMap<String, Any>? = null, sync: Boolean = true) {
        val activity = ActivityRequest(
            events = listOf(EventRequest(name = name, attributes = properties)),
            userId = session.user,
            accountId = config.accountId
        )

        // this will respond with qualified experiences, if applicable
        val experiences = repository.trackActivity(activity, sync)

        if (sync && experiences.isNotEmpty()) {
            // note: by default we just show the first experience, but will need to revisit and allow
            // for showing secondary qualified experience if the first fails to load for some reason
            experienceRenderer.show(experiences.first())
        }
    }
}
