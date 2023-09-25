package com.appcues.di.definition

internal class ScopedDefinition<T : Any>(factory: (DefinitionParams) -> T) : Definition<T>(factory) {

    private var instance: T? = null

    override fun get(params: DefinitionParams): T {
        return instance ?: factory.invoke(params).also { instance = it }
    }
}
