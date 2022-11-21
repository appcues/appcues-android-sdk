package com.appcues.trait

import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.KeyholeTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StepAnimationTrait
import com.appcues.trait.appcues.StickyContentTrait
import com.appcues.trait.appcues.TooltipTrait
import org.koin.dsl.ScopeDSL

internal object TraitKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped {
            TraitRegistry(
                scope = get(),
                logcues = get()
            )
        }

        factory { params ->
            BackdropTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            StepAnimationTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            KeyholeTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            ModalTrait(
                config = params.getOrNull(),
                scope = get(),
                context = get(),
            )
        }

        factory { params ->
            SkippableTrait(
                config = params.getOrNull(),
                experienceRenderer = get(),
                appcuesCoroutineScope = get(),
            )
        }

        factory { params ->
            CarouselTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            PagingDotsTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            StickyContentTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            TooltipTrait(
                config = params.getOrNull(),
                scope = get(),
            )
        }

        factory { params ->
            BackgroundContentTrait(
                config = params.getOrNull(),
                level = params.get(),
            )
        }
    }
}
