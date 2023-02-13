package com.appcues.data.mapper

import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.ExperienceTraitLevel

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
