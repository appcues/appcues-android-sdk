package com.appcues.di.scope

import com.appcues.di.definition.DefinitionParams
import com.appcues.di.definition.FactoryDefinition
import com.appcues.di.definition.ScopedDefinition

internal class AppcuesScopeDSL(val scope: AppcuesScope) {

    inline fun <reified T : Any> get(params: DefinitionParams = DefinitionParams()): T {
        return scope.get(T::class, params)
    }

    inline fun <reified T : Any> scoped(noinline factory: (params: DefinitionParams) -> T) {
        scope.define(T::class, ScopedDefinition(factory))
    }

    inline fun <reified T : Any> factory(noinline factory: (params: DefinitionParams) -> T) {
        scope.define(T::class, FactoryDefinition(factory))
    }
}
