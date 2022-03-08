package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault

internal class TrackEventAction(
    override val config: AppcuesConfigMap,
) : ExperienceAction {

    private val eventName = config.getConfigOrDefault<String?>("eventName", null)

    override suspend fun execute(appcues: Appcues) {
        if (!eventName.isNullOrEmpty()) {
            appcues.track(eventName)
        }
    }
}
