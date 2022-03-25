package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig

internal class LaunchExperienceAction(
    override val config: AppcuesConfigMap,
) : ExperienceAction {

    companion object {
        const val TYPE = "@appcues/launch-experience"
    }

    private val experienceId: String? = config.getConfig("experienceID")

    override suspend fun execute(appcues: Appcues) {
        if (experienceId != null) {
            appcues.show(experienceId)
        }
    }
}
