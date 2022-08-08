package com.appcues.data.mapper

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.ExperienceTrait.ExperienceTraitLevel
import java.util.UUID

internal fun List<ExperienceTrait>.mergeTraits(others: List<ExperienceTrait>, level: ExperienceTraitLevel): List<ExperienceTrait> {
    return mutableListOf<ExperienceTrait>().apply {
        addAll(this@mergeTraits)

        others.filter { other -> other.level > level }
            .forEach { other -> if (none { it.type == other.type }) add(other) }
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
