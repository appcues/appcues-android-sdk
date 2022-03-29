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

internal fun HashMap<UUID, List<ActionResponse>>?.mergeActions(
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
