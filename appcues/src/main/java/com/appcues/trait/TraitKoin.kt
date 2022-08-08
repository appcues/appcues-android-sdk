package com.appcues.trait

import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.BackgroundContentTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import com.appcues.trait.appcues.StickyContentTrait
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
            ModalTrait(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            SkippableTrait(
                config = params.getOrNull(),
                stateMachine = get(),
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
                stepContentMapper = get(),
            )
        }

        factory { params ->
            BackgroundContentTrait(
                config = params.getOrNull(),
                level = params.get(),
                stepContentMapper = get(),
            )
        }
    }
}
