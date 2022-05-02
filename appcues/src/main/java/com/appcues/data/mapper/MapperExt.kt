package com.appcues.data.mapper

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.data.remote.response.trait.TraitResponse
import java.util.UUID

internal fun List<TraitResponse>.mergeTraits(other: List<TraitResponse>): List<TraitResponse> {
    return mutableListOf<TraitResponse>().apply {
        addAll(this@mergeTraits)

        other.forEach { trait ->
            if (none { it.type == trait.type }) {
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
