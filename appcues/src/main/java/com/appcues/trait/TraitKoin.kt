package com.appcues.trait

import com.appcues.di.KoinScopePlugin
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
import org.koin.dsl.ScopeDSL

internal object TraitKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { TraitRegistry(get(), get()) }

        factory { params -> BackdropTrait(params.getOrNull()) }
        factory { params -> StepAnimationTrait(params.getOrNull()) }
        factory { params -> TargetElementTrait(params.getOrNull()) }
        factory { params -> TargetRectangleTrait(params.getOrNull()) }
        factory { params -> BackdropKeyholeTrait(params.getOrNull()) }
        factory { params -> ModalTrait(params.getOrNull(), params.get(), get()) }
        factory { params -> TooltipTrait(params.getOrNull(), params.get(), get()) }
        factory { params -> EmbedTrait(params.getOrNull(), params.get(), get()) }
        factory { params -> SkippableTrait(params.getOrNull(), params.get(), get(), get()) }
        factory { params -> CarouselTrait(params.getOrNull()) }
        factory { params -> PagingDotsTrait(params.getOrNull()) }
        factory { params -> TargetInteractionTrait(params.getOrNull(), params.get(), get(), get()) }
        factory { params -> BackgroundContentTrait(params.getOrNull(), params.get()) }
    }
}
