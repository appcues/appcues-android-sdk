package com.appcues.data.mapper.trait

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry

internal class TraitsMapper(
    private val traitRegistry: TraitRegistry
) {

    fun map(from: List<LeveledTraitResponse>): List<ExperienceTrait> {
        return arrayListOf<ExperienceTrait>().apply {
            from.forEach {
                traitRegistry[it.first.type]?.also { factory ->
                    add(factory.invoke(it.first.config, it.second))
                }
            }
        }
    }
}
