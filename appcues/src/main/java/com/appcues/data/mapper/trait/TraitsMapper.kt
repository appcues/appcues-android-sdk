package com.appcues.data.mapper.trait

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.trait.BackdropDecoratingTrait
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

            // sort BackdropDecoratingTrait items so that the one with greater priority is at the bottom of the list
            // during composition ApplyBackgroundDecoratingTraits applies traits in reversed order. this way the trait
            // with greater is applied first (useful for enforcing order between backdrop and keyhole trait)
            sortBy { if (it is BackdropDecoratingTrait) it.priority else -1 }
        }
    }
}
