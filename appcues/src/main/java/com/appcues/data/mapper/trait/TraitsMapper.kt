package com.appcues.data.mapper.trait

import com.appcues.data.remote.response.trait.TraitResponse
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry

internal class TraitsMapper(
    private val traitRegistry: TraitRegistry
) {

    fun map(from: List<TraitResponse>): List<ExperienceTrait> {
        return arrayListOf<ExperienceTrait>().apply {
            from.forEach {
                traitRegistry[it.type]?.also { factory ->
                    add(factory.invoke(it.config))
                }
            }
        }
    }
}
