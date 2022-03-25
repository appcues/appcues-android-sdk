package com.appcues.trait

import com.appcues.AppcuesConfig
import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.CarouselTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.PagingDotsTrait
import com.appcues.trait.appcues.SkippableTrait
import org.koin.dsl.ScopeDSL

internal object TraitKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
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
                scope = get(),
                context = get(),
            )
        }

        factory { params ->
            SkippableTrait(
                config = params.getOrNull(),
                stateMachine = get(),
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
    }
}
