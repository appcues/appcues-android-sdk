package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.analytics.Analytics
import com.appcues.data.model.AppcuesConfigMap

internal class UpdateProfileAction(
    override val config: AppcuesConfigMap,
    private val analytics: Analytics,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/update-profile"
    }

    override suspend fun execute() {
        if (config != null) {
            analytics.updateProfile(properties = config, isInternal = false)
        }
    }
}
