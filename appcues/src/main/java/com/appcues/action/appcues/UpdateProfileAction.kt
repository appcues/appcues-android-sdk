package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.Storage
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap

internal class UpdateProfileAction(
    override val config: AppcuesConfigMap,
    private val storage: Storage,
    private val appcues: Appcues,
) : ExperienceAction {

    companion object {

        const val TYPE = "@appcues/update-profile"
    }

    override suspend fun execute() {
        if (config != null) {
            appcues.identify(storage.userId, properties = config)
        }
    }
}
