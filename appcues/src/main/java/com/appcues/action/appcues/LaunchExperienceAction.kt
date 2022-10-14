package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfig

internal class LaunchExperienceAction(
    override val config: AppcuesConfigMap,
) : ExperienceAction, MetadataSettingsAction {

    constructor(experienceId: String) : this(
        hashMapOf<String, Any>("experienceID" to experienceId)
    )

    companion object {

        const val TYPE = "@appcues/launch-experience"
    }

    private val experienceId: String? = config.getConfig("experienceID")

    override val category = "internal"

    override val destination = experienceId ?: String()

    override suspend fun execute(appcues: Appcues) {
        if (experienceId != null) {
            appcues.show(experienceId)
        }
    }
}
