package com.appcues.di.definition

internal abstract class Definition<T : Any>(protected val factory: (DefinitionParams) -> T) {

    abstract fun get(params: DefinitionParams): T
}
