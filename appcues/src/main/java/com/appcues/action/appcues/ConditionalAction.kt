package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.action.ActionRegistry
import com.appcues.action.ExperienceAction
import com.appcues.action.ExperienceActionQueueTransforming
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.Clause
import com.appcues.data.model.RenderContext
import com.appcues.data.model.getConfigConditionalChecks
import com.appcues.data.remote.appcues.response.action.ActionResponse
import com.appcues.logging.Logcues
import com.appcues.ui.ExperienceRenderer
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class Check(
    val condition: Clause?,
    val actions: List<ActionResponse>
)

internal class ConditionalAction(
    override val config: AppcuesConfigMap,
    private val renderContext: RenderContext,
    private val experienceRenderer: ExperienceRenderer,
    private val actionRegistry: ActionRegistry,
    private val logcues: Logcues,
) : ExperienceActionQueueTransforming {

    companion object {

        const val TYPE = "@appcues/conditional"
    }

    private val checks = config.getConfigConditionalChecks("checks")

    override fun transformQueue(queue: List<ExperienceAction>, index: Int, appcues: Appcues): List<ExperienceAction> {
        val stepState = experienceRenderer.getState(renderContext).let { it?.currentExperience?.surveyState() } ?: return queue

        // Find the first check that evaluates to true
        val matchingCheck = checks?.find { check ->
            check.condition?.evaluate(stepState) != false
        }

        return if (matchingCheck != null) {
            val newQueue = queue.toMutableList()
            val experienceActions = matchingCheck.actions.mapNotNull { actionResponse ->
                actionRegistry[actionResponse.type]?.invoke(actionResponse.config, renderContext)
            }
            
            newQueue.removeAt(index)
            newQueue.addAll(index, experienceActions)
            val conditionDesc = matchingCheck.condition?.toString() ?: "else"
            logcues.debug("$TYPE satisfied $conditionDesc, adding ${experienceActions.count()} action(s) to queue")
            newQueue
        } else {
            logcues.debug("$TYPE no checks satisfied")
            queue
        }
    }

    override suspend fun execute() {
        // This action only transforms the queue
    }
}
