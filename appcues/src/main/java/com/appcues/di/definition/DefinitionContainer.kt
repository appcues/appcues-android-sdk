package com.appcues.di.definition

import kotlin.reflect.KClass

internal class DefinitionContainer {

    private val definitions = hashMapOf<KClass<*>, Definition<*>>()

    fun <T : Any> get(clazz: KClass<T>, params: DefinitionParams): T {
        val definition = definitions[clazz] ?: throw DefinitionException("definition for $clazz not present")

        @Suppress("UNCHECKED_CAST")
        return (definition.get(params) as? T) ?: throw DefinitionException("cast of instance to $clazz failed")
    }

    fun <T : Any> define(clazz: KClass<T>, definition: Definition<T>) {
        if (definitions.contains(clazz)) throw DefinitionException("definition already registered for class $clazz")

        definitions[clazz] = definition
    }
}
