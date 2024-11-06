package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigOrDefault
import com.appcues.ui.ExperienceRenderer

internal class CloseAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experienceRenderer: ExperienceRenderer,
) : ExperienceAction, MetadataSettingsAction {

    companion object {

        const val TYPE = "@appcues/close"

        private fun buildConfigMap(markComplete: Boolean): AppcuesConfigMap {
            return mutableMapOf<String, Any>().apply {
                put("markComplete", markComplete)
            }
        }
    }

    constructor(
        renderContext: RenderContext,
        experienceRenderer: ExperienceRenderer,
        markComplete: Boolean
    ) : this(buildConfigMap(markComplete), renderContext, experienceRenderer)

    private val markComplete = config.getConfigOrDefault("markComplete", false)

    override val category = "internal"

    override val destination = "end-experience"

    override suspend fun execute() {
        experienceRenderer.dismiss(renderContext, markComplete = markComplete, destroyed = false)
    }
}
