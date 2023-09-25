package com.appcues.di.definition

internal class FactoryDefinition<T : Any>(factory: (DefinitionParams) -> T) : Definition<T>(factory) {

    override fun get(params: DefinitionParams): T {
        return factory.invoke(params)
    }
}
