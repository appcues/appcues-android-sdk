package com.appcues.data.model

import java.util.UUID

internal data class Experience(
    val id: UUID,
    val name: String,
    val stepContainers: List<StepContainer>
) {

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
}
