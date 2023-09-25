package com.appcues.di.scope

import android.content.Context
import com.appcues.di.definition.Definition
import com.appcues.di.definition.DefinitionContainer
import com.appcues.di.definition.DefinitionParams
import java.util.UUID
import kotlin.reflect.KClass

internal data class AppcuesScope(
    val context: Context,
    val scopeId: UUID = UUID.randomUUID()
) {

    private val container = DefinitionContainer()

    fun <T : Any> define(clazz: KClass<T>, definition: Definition<T>) {
        container.define(clazz, definition)
    }

    fun <T : Any> inject(clazz: KClass<T>, params: DefinitionParams = DefinitionParams()): Lazy<T> =
        lazy(LazyThreadSafetyMode.SYNCHRONIZED) { get(clazz, params) }

    fun <T : Any> get(clazz: KClass<T>, params: DefinitionParams = DefinitionParams()): T = container.get(clazz, params)
}
