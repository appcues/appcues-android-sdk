package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.AppcuesConfigMap

internal class UpdateProfileAction(
    override val config: AppcuesConfigMap,
    private val analyticsTracker: AnalyticsTracker,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/update-profile"
    }

    override suspend fun execute() {
        if (config != null) {
            analyticsTracker.identify(config)
        }
    }
}
