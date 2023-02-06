package com.appcues.trait

import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.BackdropKeyholeTrait
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StepAnimationTrait
import com.appcues.trait.appcues.TargetElementTrait
import com.appcues.trait.appcues.TargetRectangleTrait
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
            TargetElementTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            TargetRectangleTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            BackdropKeyholeTrait(
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
            TooltipTrait(
                config = params.getOrNull(),
                scope = get(),
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
            BackgroundContentTrait(
                config = params.getOrNull(),
                level = params.get(),
            )
        }
    }
}
