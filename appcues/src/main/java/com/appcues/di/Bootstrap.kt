package com.appcues.di

import android.content.Context
import com.appcues.AppcuesConfig
import com.appcues.MainModule
import com.appcues.analytics.AnalyticsModule
import com.appcues.data.local.DataLocalModule
import com.appcues.data.mapper.DataMapperModule
import com.appcues.data.remote.DataRemoteModule
import com.appcues.debugger.DebuggerModule
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.util.ContextWrapper

internal object Bootstrap {

    private val scopes = arrayListOf<AppcuesScope>()

    private val modules = listOf(
        MainModule,
        AnalyticsModule,
        DataRemoteModule,
        DataMapperModule,
        DataLocalModule,
        DebuggerModule
    )

    fun createScope(context: Context, config: AppcuesConfig): AppcuesScope {
        return start(ContextWrapper(context), modules) {
            scoped { config }
        }
    }

    fun start(
        contextWrapper: ContextWrapper,
        modules: List<AppcuesModule> = arrayListOf(),
        scopeDSL: (AppcuesScopeDSL.() -> Unit)? = null
    ): AppcuesScope {
        val scope = AppcuesScope()

        AppcuesScopeDSL(scope).run {
            scoped { scope }
            scoped { contextWrapper }
            scoped<Context> { contextWrapper.getApplication() }

            scopeDSL?.invoke(this)

            modules.forEach { with(it) { install() } }
        }

        scopes.add(scope)

        return scope
    }

    fun get(scopeId: String): AppcuesScope? {
        return scopes.firstOrNull { it.scopeId.toString() == scopeId }
    }
}
