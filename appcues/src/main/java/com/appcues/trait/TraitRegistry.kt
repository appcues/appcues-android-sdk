package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.logging.Logcues
import com.appcues.trait.appcues.BackdropKeyholeTrait
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.EmbedTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StepAnimationTrait
import com.appcues.trait.appcues.TargetElementTrait
import com.appcues.trait.appcues.TargetInteractionTrait
import com.appcues.trait.appcues.TargetRectangleTrait
import com.appcues.trait.appcues.TooltipTrait
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.collections.set

internal typealias TraitFactoryBlock = (
    config: AppcuesConfigMap,
    level: ExperienceTraitLevel,
    renderContext: RenderContext
) -> ExperienceTrait

internal class TraitRegistry(
    override val scope: Scope,
    private val logcues: Logcues
) : KoinScopeComponent {

    private val actions: MutableMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        register(ModalTrait.TYPE) { config, _, context -> get<ModalTrait> { parametersOf(config, context) } }
        register(TooltipTrait.TYPE) { config, _, context -> get<TooltipTrait> { parametersOf(config, context) } }
        register(EmbedTrait.TYPE) { config, _, context -> get<EmbedTrait> { parametersOf(config, context) } }
        register(TargetInteractionTrait.TYPE) { config, _, context -> get<TargetInteractionTrait> { parametersOf(config, context) } }
        register(SkippableTrait.TYPE) { config, _, context -> get<SkippableTrait> { parametersOf(config, context) } }

        register(BackgroundContentTrait.TYPE) { config, level -> get<BackgroundContentTrait> { parametersOf(config, level) } }

        register(StepAnimationTrait.TYPE) { config, _ -> get<StepAnimationTrait> { parametersOf(config) } }
        register(TargetElementTrait.TYPE) { config, _ -> get<TargetElementTrait> { parametersOf(config) } }
        register(TargetRectangleTrait.TYPE) { config, _ -> get<TargetRectangleTrait> { parametersOf(config) } }
        register(BackdropTrait.TYPE) { config, _ -> get<BackdropTrait> { parametersOf(config) } }
        register(BackdropKeyholeTrait.TYPE) { config, _ -> get<BackdropKeyholeTrait> { parametersOf(config) } }
        register(CarouselTrait.TYPE) { config, _ -> get<CarouselTrait> { parametersOf(config) } }
        register(PagingDotsTrait.TYPE) { config, _ -> get<PagingDotsTrait> { parametersOf(config) } }
    }

    operator fun get(key: String): TraitFactoryBlock? {
        return actions[key]
    }

    fun register(type: String, traitFactory: (config: Map<String, Any>?, level: ExperienceTraitLevel) -> ExperienceTrait) {
        register(type) { config, level, _ -> traitFactory(config, level) }
    }

    private fun register(type: String, factory: TraitFactoryBlock) {
        if (actions.contains(type)) {
            logcues.error(AppcuesDuplicateTraitException(type))
        } else {
            actions[type] = factory
        }
    }

    private class AppcuesDuplicateTraitException(type: String) :
        Exception("Fail to register trait $type: Trait already registered")
}
