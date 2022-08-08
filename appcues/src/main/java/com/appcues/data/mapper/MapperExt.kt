package com.appcues.data.mapper

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel
import java.util.UUID

internal typealias LeveledTraitResponse = Pair<TraitResponse, ExperienceTraitLevel>

internal fun List<LeveledTraitResponse>.mergeTraits(other: List<LeveledTraitResponse>): List<LeveledTraitResponse> {
    return mutableListOf<LeveledTraitResponse>().apply {
        addAll(this@mergeTraits)

        other.forEach { trait ->
            if (none { it.first.type == trait.first.type }) {
                add(trait)
            }
        }
    }
}

internal fun Map<UUID, List<ActionResponse>>.mergeActions(
    other: Map<UUID, List<ActionResponse>>?
): Map<UUID, List<ActionResponse>> {
    return when {
        // other is null, return this
        other == null -> this
        // this is empty, return other
        isEmpty() -> other
        // this is not empty and other is not null, return merge of both
        else -> hashMapOf<UUID, List<ActionResponse>>().also { hashMap ->
            forEach {
                hashMap[it.key] = it.value.toMutableList().apply {
                    other[it.key]?.let { keyActions -> addAll(keyActions) }
                }
            }
        }
    }
}
