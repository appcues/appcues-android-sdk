package com.appcues.data.model

import com.appcues.action.ExperienceAction
import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val stepContainers: List<StepContainer>,
    val published: Boolean,
    val priority: ExperiencePriority,
    val type: String?,
    val renderContext: RenderContext,
    val publishedAt: Long?,
    val localeId: String?,
    val localeName: String?,
    val experiment: Experiment?,
    val completionActions: List<ExperienceAction>,
    val trigger: ExperienceTrigger,
    val requestId: UUID? = null,
    val error: String? = null,
    var renderErrorId: UUID? = null,
) {

    // a unique identifier for this instance of the Experience, for comparison purposes, in the
    // situation where multiple experiences with the same `id` may be initiated simultaneously
    val instanceId: UUID = UUID.randomUUID()

    // will run once when creating the experience
    val flatSteps: List<Step> = stepContainers.flatMap { it.steps }

    val groupLookup: Map<Int, Int> = hashMapOf<Int, Int>().apply {
        var stepIndex = 0
        stepContainers.forEachIndexed { index, stepContainer ->
            repeat(stepContainer.steps.size) {
                put(stepIndex++, index)
            }
        }
    }

    val stepIndexLookup: Map<Int, Int> = hashMapOf<Int, Int>().apply {
        flatSteps.forEachIndexed { stepIndex, step ->
            stepContainers.forEach {
                val index = it.steps.indexOf(step)
                if (index >= 0) {
                    put(stepIndex, index)
                }
            }
        }
    }
}
