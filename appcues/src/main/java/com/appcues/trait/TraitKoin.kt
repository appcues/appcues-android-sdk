package com.appcues.trait

import com.appcues.AppcuesConfig
import com.appcues.di.KoinScopePlugin
import com.appcues.trait.appcues.AppcuesBackdropTrait
import com.appcues.trait.appcues.AppcuesModalTrait
import org.koin.dsl.ScopeDSL

internal object TraitKoin : KoinScopePlugin {

    override fun installIn(koinScope: ScopeDSL, scopeId: String, config: AppcuesConfig) {
        with(koinScope) {
            scoped {
                TraitRegistry(
                    scopeId = scopeId,
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
                    scopeId = scopeId,
                    context = get(),
                )
            }
        }
    }
}
