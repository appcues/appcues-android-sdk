package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.scope.AppcuesScope
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
import kotlin.collections.set

internal typealias TraitFactoryBlock = (
    config: AppcuesConfigMap,
    level: ExperienceTraitLevel,
    renderContext: RenderContext
) -> ExperienceTrait

internal class TraitRegistry(
    override val scope: AppcuesScope,
    private val logcues: Logcues
) : AppcuesComponent {

    private val actions: MutableMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        register(ModalTrait.TYPE) { config, _, context -> ModalTrait(config, context, get()) }
        register(TooltipTrait.TYPE) { config, _, context -> TooltipTrait(config, context, get()) }
        register(EmbedTrait.TYPE) { config, _, context -> EmbedTrait(config, context, get()) }
        register(TargetInteractionTrait.TYPE) { config, _, context -> TargetInteractionTrait(config, context, get(), get()) }
        register(SkippableTrait.TYPE) { config, _, context -> SkippableTrait(config, context, get(), get()) }

        register(BackgroundContentTrait.TYPE) { config, level -> BackgroundContentTrait(config, level) }

        register(StepAnimationTrait.TYPE) { config, _ -> StepAnimationTrait(config) }
        register(TargetElementTrait.TYPE) { config, _ -> TargetElementTrait(config) }
        register(TargetRectangleTrait.TYPE) { config, _ -> TargetRectangleTrait(config) }
        register(BackdropTrait.TYPE) { config, _ -> BackdropTrait(config) }
        register(BackdropKeyholeTrait.TYPE) { config, _ -> BackdropKeyholeTrait(config) }
        register(CarouselTrait.TYPE) { config, _ -> CarouselTrait(config) }
        register(PagingDotsTrait.TYPE) { config, _ -> PagingDotsTrait(config) }
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
