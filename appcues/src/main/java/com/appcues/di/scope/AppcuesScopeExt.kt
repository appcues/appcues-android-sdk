package com.appcues.di.scope

import com.appcues.di.definition.DefinitionParams

internal inline fun <reified T : Any> AppcuesScope.inject(params: DefinitionParams = DefinitionParams()): Lazy<T> {
    return inject(T::class, params)
}

internal inline fun <reified T : Any> AppcuesScope.get(params: DefinitionParams = DefinitionParams()): T {
    return get(T::class, params)
}
