package com.appcues.trait

import com.appcues.AppcuesConfig
import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.AppcuesBackdropTrait
import com.appcues.trait.appcues.AppcuesModalTrait
import org.koin.dsl.ScopeDSL

internal object TraitKoin : KoinScopePlugin {

    override fun installIn(koinScope: ScopeDSL, config: AppcuesConfig) {
        with(koinScope) {
            scoped {
                TraitRegistry(
                    scopeId = id,
                    logcues = get()
                )
            }

            factory { params ->
                AppcuesBackdropTrait(
                    config = params.getOrNull(),
                    stateMachine = get(),
                )
            }

            factory { params ->
                AppcuesModalTrait(
                    config = params.getOrNull(),
                    scopeId = id,
                    context = get(),
                )
            }
        }
    }
}
