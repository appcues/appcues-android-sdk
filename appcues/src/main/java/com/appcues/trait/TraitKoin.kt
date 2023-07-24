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

        factory { BackdropTrait(it[0]) }
        factory { StepAnimationTrait(it[0]) }
        factory { TargetElementTrait(it[0]) }
        factory { TargetRectangleTrait(it[0]) }
        factory { BackdropKeyholeTrait(it[0]) }
        factory { ModalTrait(it[0], it[1], get()) }
        factory { TooltipTrait(it[0], it[1], get()) }
        factory { EmbedTrait(it[0], it[1], get()) }
        factory { SkippableTrait(it[0], it[1], get(), get()) }
        factory { CarouselTrait(it[0]) }
        factory { PagingDotsTrait(it[0]) }
        factory { TargetInteractionTrait(it[0], it[1], get(), get()) }
        factory { BackgroundContentTrait(it[0], it[1]) }
    }
}
