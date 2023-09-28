package com.appcues.data.mapper.trait

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.model.RenderContext
import com.appcues.di.definition.DefinitionException
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry
import com.appcues.trait.appcues.BackdropKeyholeTrait

internal class TraitsMapper(
    private val traitRegistry: TraitRegistry
) {

    fun map(renderContext: RenderContext, from: List<LeveledTraitResponse>): List<ExperienceTrait> {
        return arrayListOf<ExperienceTrait>().apply {
            from.forEach {
                traitRegistry[it.first.type]?.also { factory ->
                    try {
                        add(factory.invoke(it.first.config, it.second, renderContext))
                    } catch (ex: DefinitionException) {
                        // since Traits are loaded through DI framework, we catch any issue
                        // with definitions during creation, then hopefully get the underlying
                        // AppcuesTraitException as the cause and throw that instead. This is
                        // because higher level mapping code handles the trait exceptions and
                        // reports proper ExperienceErrors
                        throw ex.cause ?: ex
                    }
                }
            }

            // ensure BackdropKeyholeTrait is applied first
            sortBy { it is BackdropKeyholeTrait }
        }
    }
}
