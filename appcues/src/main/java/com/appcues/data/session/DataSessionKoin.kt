package com.appcues.data.session

import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object DataSessionKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped {
            PrefSessionLocalSource(
                context = get(),
                config = get(),
            )
        }
    }
}
