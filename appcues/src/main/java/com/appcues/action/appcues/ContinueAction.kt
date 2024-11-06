package com.appcues.action.appcues

import com.appcues.action.ExperienceAction
import com.appcues.action.MetadataSettingsAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference
import com.appcues.data.model.StepReference.StepGroupPageIndex
import com.appcues.data.model.StepReference.StepId
import com.appcues.data.model.StepReference.StepIndex
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.data.model.getConfig
import com.appcues.data.model.getConfigInt
import com.appcues.ui.ExperienceRenderer
import java.util.UUID

internal class ContinueAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experienceRenderer: ExperienceRenderer,
) : ExperienceAction, MetadataSettingsAction {

    companion object {

        const val TYPE = "@appcues/continue"

        private fun buildConfigMap(stepReference: StepReference): AppcuesConfigMap {
            return mutableMapOf<String, Any>().apply {
                when (stepReference) {
                    is StepId -> put("stepID", stepReference.id)
                    is StepIndex -> put("index", stepReference.index)
                    is StepOffset -> put("offset", stepReference.offset)
                    // StepGroupPageIndex is unsupported by ContinueAction
                    is StepGroupPageIndex -> Unit
                }
            }
        }
    }

    constructor(
        renderContext: RenderContext,
        experienceRenderer: ExperienceRenderer,
        stepReference: StepReference,
    ) : this(buildConfigMap(stepReference), renderContext, experienceRenderer)

    private val index = config.getConfigInt("index")

    private val offset = config.getConfigInt("offset") ?: 1

    private val id = config.getConfig<String>("stepID")

    private val stepReference: StepReference
        get() = when {
            index != null -> StepIndex(index)
            id != null -> StepId(UUID.fromString(id))
            else -> StepOffset(offset)
        }

    override val category: String = "internal"

    override val destination: String = stepReference.destination

    override suspend fun execute() {
        experienceRenderer.show(renderContext, stepReference)
    }
}
