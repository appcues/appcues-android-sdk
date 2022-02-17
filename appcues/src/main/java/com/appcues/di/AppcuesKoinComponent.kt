package com.appcues.di

import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope

interface AppcuesKoinComponent : KoinScopeComponent {

    val scopeId: String

    override val scope: Scope
        get() = AppcuesKoinContext.getScope(scopeId)

    override fun getKoin() = AppcuesKoinContext.koin
}
