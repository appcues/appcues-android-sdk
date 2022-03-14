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

    fun map(
        from: StepResponse,
        stepContainerActions: HashMap<UUID, List<ActionResponse>>?,
        experienceActions: HashMap<UUID, List<ActionResponse>>?
    ): Step {
        return Step(
            id = from.id,
            content = stepContentMapper.map(
                from.content,
                from.actions
                    .mergeActions(stepContainerActions)
                    .mergeActions(experienceActions),
            ),
            stepTraits = traitsMapper.map(from.traits).filterIsInstance(StepDecoratingTrait::class.java),
        )
    }

    private fun HashMap<UUID, List<ActionResponse>>?.mergeActions(
        other: HashMap<UUID, List<ActionResponse>>?
    ): HashMap<UUID, List<ActionResponse>>? {
        return when {
            // this is not null and not empty and other is null, return this
            this != null && isNotEmpty() && other == null -> this
            // this is null or empty and other is not null, return other
            this.isNullOrEmpty() && other != null -> other
            // this is not null and not empty and other is not null, return merge of both
            this != null && isNotEmpty() && other != null -> hashMapOf<UUID, List<ActionResponse>>().also { hashMap ->
                forEach {
                    hashMap[it.key] = it.value.toMutableList().apply {
                        other[it.key]?.let { keyActions -> addAll(keyActions) }
                    }
                }
            }
            // both are null, both are empty, etc..
            else -> null
        }
    }
}
