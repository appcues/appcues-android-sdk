package com.appcues.trait

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
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

internal fun Scope.stepAnimationTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<StepAnimationTrait> { parametersOf(config) } }
}

internal fun Scope.targetElementTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<TargetElementTrait> { parametersOf(config) } }
}

internal fun Scope.targetRectangleTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<TargetRectangleTrait> { parametersOf(config) } }
}

internal fun Scope.backdropTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<BackdropTrait> { parametersOf(config) } }
}

internal fun Scope.backdropKeyholeTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<BackdropKeyholeTrait> { parametersOf(config) } }
}

internal fun Scope.carouselTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<CarouselTrait> { parametersOf(config) } }
}

internal fun Scope.pagingDotsTraitFactory(): TraitFactoryBlock {
    return { config, _, _ -> get<PagingDotsTrait> { parametersOf(config) } }
}

internal fun Scope.backgroundContentTraitFactory(): TraitFactoryBlock {
    return { config, level, _ -> get<BackgroundContentTrait> { parametersOf(config, level) } }
}

internal fun Scope.modalTraitFactory(): TraitFactoryBlock {
    return { config, _, renderContext -> get<ModalTrait> { parametersOf(config, renderContext) } }
}

internal fun Scope.tooltipTraitFactory(): TraitFactoryBlock {
    return { config, _, renderContext -> get<TooltipTrait> { parametersOf(config, renderContext) } }
}

internal fun Scope.skippableTraitFactory(): TraitFactoryBlock {
    return { config, _, renderContext -> get<SkippableTrait> { parametersOf(config, renderContext) } }
}

internal fun Scope.targetInteractionTraitFactory(): TraitFactoryBlock {
    return { config, _, renderContext -> get<TargetInteractionTrait> { parametersOf(config, renderContext) } }
}
