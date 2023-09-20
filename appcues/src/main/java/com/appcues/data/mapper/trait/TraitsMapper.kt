package com.appcues.data.mapper.trait

import com.appcues.data.mapper.LeveledTraitResponse
import com.appcues.data.model.RenderContext
import com.appcues.trait.ExperienceTrait
import com.appcues.trait.TraitRegistry
import com.appcues.trait.appcues.BackdropKeyholeTrait
import org.koin.core.error.InstanceCreationException

internal class TraitsMapper(
    private val traitRegistry: TraitRegistry
) {

    fun map(renderContext: RenderContext, from: List<LeveledTraitResponse>): List<ExperienceTrait> {
        return arrayListOf<ExperienceTrait>().apply {
            from.forEach {
                traitRegistry[it.first.type]?.also { factory ->
                    try {
                        add(factory.invoke(it.first.config, it.second, renderContext))
                    } catch (ex: InstanceCreationException) {
                        // since Traits are loaded through Koin, we catch any issue
                        // with Koin instance creation here, then hopefully get the underlying
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
