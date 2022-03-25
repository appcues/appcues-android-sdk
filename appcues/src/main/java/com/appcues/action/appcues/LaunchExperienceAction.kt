package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault

internal class LaunchExperienceAction(
    override val config: AppcuesConfigMap,
) : ExperienceAction {

    companion object {
        const val NAME = "@appcues/launch-experience"
    }

    private val experienceId = config.getConfigOrDefault<String?>("experienceID", null)

    override suspend fun execute(appcues: Appcues) {
        if (experienceId != null) {
            appcues.show(experienceId)
        }
    }
}
