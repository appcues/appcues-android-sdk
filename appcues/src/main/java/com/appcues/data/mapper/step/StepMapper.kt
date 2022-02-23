package com.appcues.data.mapper.step

import com.appcues.data.mapper.trait.TraitMapper
import com.appcues.data.model.Step
import com.appcues.data.model.Trait
import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.step.StepResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal class StepMapper(
    private val stepContentMapper: StepContentMapper,
    private val traitMapper: TraitMapper = TraitMapper()
) {

    fun map(from: StepResponse, actions: HashMap<UUID, List<ActionResponse>>?) = Step(
        id = from.id,
        content = stepContentMapper.map(from.content, from.actions.mergeActions(actions)),
        traits = from.traits.mapToTrait { traitMapper.map(it) },
    )

    private fun List<TraitResponse>.mapToTrait(transform: (TraitResponse) -> Trait) = map { transform(it) }

    private fun HashMap<UUID, List<ActionResponse>>?.mergeActions(
        actions: HashMap<UUID, List<ActionResponse>>?
    ): HashMap<UUID, List<ActionResponse>>? {
        return when {
            this != null && actions == null -> this
            this == null && actions != null -> actions
            this != null && actions != null -> {
                hashMapOf<UUID, List<ActionResponse>>().also { hashMap ->
                    forEach {
                        hashMap[it.key] = it.value.toMutableList().apply {
                            actions[it.key]?.let { keyActions -> addAll(keyActions) }
                        }
                    }
                }
            }
            else -> null
        }
    }
}
