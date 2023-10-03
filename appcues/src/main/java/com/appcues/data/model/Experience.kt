package com.appcues.data.model

import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action.Trigger.NAVIGATE
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.PresentingTrait
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

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

    // Gets any actions defined on the step group container for the "navigate" trigger. These are the
    // actions that should be executed before presenting this group's container.
    fun getNavigationActions(stepContainerIndex: Int): List<ExperienceAction> {
        val stepGroup = stepContainers.getOrNull(stepContainerIndex) ?: return arrayListOf()
        return stepGroup.actions[stepGroup.id]?.filter { it.on == NAVIGATE }?.map { it.experienceAction } ?: emptyList()
    }

    fun areStepsFromDifferentGroup(stepIndexOne: Int, stepIndexTwo: Int): Boolean {
        return groupLookup[stepIndexOne] != groupLookup[stepIndexTwo]
    }

    fun getPresentingTrait(flatStepIndex: Int): PresentingTrait {
        return groupLookup[flatStepIndex]?.let { stepContainers[it].presentingTrait }
            ?: throw AppcuesTraitException("cannot to find presenting trait")
    }

    @OptIn(ExperimentalContracts::class)
    fun isValidStepIndex(stepIndex: Int?): Boolean {
        contract {
            returns(true) implies (stepIndex != null)
        }

        return stepIndex != null && stepIndex >= 0 && stepIndex < flatSteps.size
    }
}
