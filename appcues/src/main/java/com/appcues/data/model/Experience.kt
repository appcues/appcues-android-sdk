package com.appcues.data.model

import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val stepContainers: List<StepContainer>,
    val published: Boolean,
) {

    // a unique identifier for this instance of the Experience, for comparison purposes, in the
    // situation where multiple experiences with the same `id` may be initiated simultaneously
    val instanceId: UUID = UUID.randomUUID()

    // will run once when creating the experience
    val flatSteps: List<Step> = stepContainers.flatMap { it.steps }

    val groupLookup: HashMap<Int, Int> = hashMapOf<Int, Int>().apply {
        var stepIndex = 0
        stepContainers.forEachIndexed { index, stepContainer ->
            repeat(stepContainer.steps.size) {
                put(stepIndex++, index)
            }
        }
    }

    val stepIndexLookup: HashMap<Int, Int> = hashMapOf<Int, Int>().apply {
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
