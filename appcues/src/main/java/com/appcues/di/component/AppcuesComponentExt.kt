package com.appcues.di.component

import com.appcues.di.definition.DefinitionParams

internal inline fun <reified T : Any> AppcuesComponent.get(vararg params: Any?): T {
    return scope.get(T::class, DefinitionParams(params.toMutableList()))
}

internal inline fun <reified T : Any> AppcuesComponent.inject(vararg params: Any?): Lazy<T> {
    return scope.inject(T::class, DefinitionParams(params.toMutableList()))
}
