package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault

internal class TrackEventAction(
    override val config: AppcuesConfigMap,
    private val appcues: Appcues,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/track"
    }

    private val eventName = config.getConfigOrDefault<String?>("eventName", null)

    override suspend fun execute() {
        if (!eventName.isNullOrEmpty()) {
            appcues.track(eventName)
        }
    }
}
