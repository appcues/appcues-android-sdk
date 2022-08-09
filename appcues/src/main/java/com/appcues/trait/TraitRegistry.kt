package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StickyContentTrait
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.collections.set

internal typealias TraitFactoryBlock = (config: AppcuesConfigMap, level: ExperienceTraitLevel) -> ExperienceTrait

internal class TraitRegistry(
    override val scope: Scope,
    private val logcues: Logcues
) : KoinScopeComponent {

    private val actions: MutableMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        register(BackdropTrait.TYPE) { config, _ -> get<BackdropTrait> { parametersOf(config) } }
        register(ModalTrait.TYPE) { config, _ -> get<ModalTrait> { parametersOf(config) } }
        register(SkippableTrait.TYPE) { config, _ -> get<SkippableTrait> { parametersOf(config) } }
        register(CarouselTrait.TYPE) { config, _ -> get<CarouselTrait> { parametersOf(config) } }
        register(PagingDotsTrait.TYPE) { config, _ -> get<PagingDotsTrait> { parametersOf(config) } }
        register(StickyContentTrait.TYPE) { config, _ -> get<StickyContentTrait> { parametersOf(config) } }
        register(BackgroundContentTrait.TYPE) { config, level -> get<BackgroundContentTrait> { parametersOf(config, level) } }
    }

    operator fun get(key: String): TraitFactoryBlock? {
        return actions[key]
    }

    fun register(type: String, factory: TraitFactoryBlock) {
        if (actions.contains(type)) {
            logcues.error(AppcuesDuplicateTraitException(type))
        } else {
            actions[type] = factory
        }
    }

    private class AppcuesDuplicateTraitException(type: String) :
        Exception("Fail to register trait $type: Trait already registered")
}
