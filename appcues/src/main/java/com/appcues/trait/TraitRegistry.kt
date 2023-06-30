package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.logging.Logcues
import com.appcues.trait.appcues.BackdropKeyholeTrait
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StepAnimationTrait
import com.appcues.trait.appcues.TargetElementTrait
import com.appcues.trait.appcues.TargetInteractionTrait
import com.appcues.trait.appcues.TargetRectangleTrait
import com.appcues.trait.appcues.TooltipTrait
import org.koin.core.component.KoinScopeComponent
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
        register(StepAnimationTrait.TYPE, scope.stepAnimationTraitFactory())
        register(TargetElementTrait.TYPE, scope.targetElementTraitFactory())
        register(TargetRectangleTrait.TYPE, scope.targetRectangleTraitFactory())
        register(BackdropTrait.TYPE, scope.backdropTraitFactory())
        register(BackdropKeyholeTrait.TYPE, scope.backdropKeyholeTraitFactory())
        register(CarouselTrait.TYPE, scope.carouselTraitFactory())
        register(PagingDotsTrait.TYPE, scope.pagingDotsTraitFactory())

        register(BackgroundContentTrait.TYPE, scope.backgroundContentTraitFactory())

        register(ModalTrait.TYPE, scope.modalTraitFactory())
        register(TooltipTrait.TYPE, scope.tooltipTraitFactory())
        register(SkippableTrait.TYPE, scope.skippableTraitFactory())
        register(TargetInteractionTrait.TYPE, scope.targetInteractionTraitFactory())
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
