package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ExperienceAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.ui.ExperienceRenderer

internal class CloseAction(
    override val config: AppcuesConfigMap,
    private val experienceRenderer: ExperienceRenderer,
) : ExperienceAction {

    companion object {
        const val TYPE = "@appcues/close"
    }

    private val markComplete = config.getConfigOrDefault("markComplete", false)

    override suspend fun execute(appcues: Appcues) {
        experienceRenderer.dismissCurrentExperience(markComplete = markComplete, destroyed = false)
    }
}
