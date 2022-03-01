package com.appcues.data.mapper.step

import com.appcues.data.mapper.trait.TraitsMapper
import com.appcues.data.model.Step
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.trait.StepDecoratingTrait
import java.util.UUID

internal class StepMapper(
    private val stepContentMapper: StepContentMapper,
    private val traitsMapper: TraitsMapper,
) {

    fun map(from: StepResponse, actions: HashMap<UUID, List<ActionResponse>>?) = Step(
        id = from.id,
        content = stepContentMapper.map(from.content, from.actions.mergeActions(actions)),
        stepTraits = traitsMapper.map(from.traits).filterIsInstance(StepDecoratingTrait::class.java),
    )

    private fun HashMap<UUID, List<ActionResponse>>?.mergeActions(
        actions: HashMap<UUID, List<ActionResponse>>?
    ): HashMap<UUID, List<ActionResponse>>? {
        return when {
            // if step actions is not null and experience action is null, return step actions
            this != null && actions == null -> this
            // if step actions is null and experience actions is not null, return experience actions
            this == null && actions != null -> actions
            // if both are not null, then merge both lists into a new hashMap
            this != null && actions != null -> hashMapOf<UUID, List<ActionResponse>>().also { hashMap ->
                forEach {
                    hashMap[it.key] = it.value.toMutableList().apply {
                        actions[it.key]?.let { keyActions -> addAll(keyActions) }
                    }
                }
            }
            // do nothing
            else -> null
        }
    }
}
