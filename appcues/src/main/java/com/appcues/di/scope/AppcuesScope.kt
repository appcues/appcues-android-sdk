package com.appcues.di.scope

import com.appcues.di.definition.Definition
import com.appcues.di.definition.DefinitionException
import com.appcues.di.definition.DefinitionParams
import java.util.UUID
import kotlin.reflect.KClass

internal data class AppcuesScope(
    val scopeId: UUID = UUID.randomUUID(),
) {

    private val definitions = hashMapOf<KClass<*>, Definition<*>>()

    fun <T : Any> define(clazz: KClass<T>, definition: Definition<T>) {
        if (definitions.contains(clazz)) throw DefinitionException("definition already registered for class $clazz")

        definitions[clazz] = definition
    }

    fun <T : Any> get(clazz: KClass<T>, params: DefinitionParams): T {
        val definition = definitions[clazz] ?: throw DefinitionException("definition for $clazz not present")

        // we know that definition get will always match T, but we need to perform
        // cast since the definition map is set as *
        @Suppress("UNCHECKED_CAST")
        return definition.get(params) as T
    }

    fun <T : Any> inject(clazz: KClass<T>, params: DefinitionParams = DefinitionParams()): Lazy<T> =
        lazy(LazyThreadSafetyMode.SYNCHRONIZED) { get(clazz, params) }
}
