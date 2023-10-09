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
import com.appcues.qualifications.QualificationsModule
import com.appcues.ui.utils.ImageLoaderWrapper
import com.appcues.util.ContextWrapper

internal object Bootstrap {

    private val scopes = arrayListOf<AppcuesScope>()

    private val modules = listOf(
        MainModule,
        AnalyticsModule,
        DataRemoteModule,
        DataMapperModule,
        DataLocalModule,
        DebuggerModule,
        QualificationsModule
    )

    fun createScope(context: Context, config: AppcuesConfig): AppcuesScope {
        return start(modules) {
            scoped { config }
            scoped { ContextWrapper(context) }
            scoped { ImageLoaderWrapper(context) }
            scoped { context.applicationContext }
        }
    }

    fun start(
        modules: List<AppcuesModule> = arrayListOf(),
        scopeDSL: (AppcuesScopeDSL.() -> Unit)? = null
    ): AppcuesScope {
        val scope = AppcuesScope()

        AppcuesScopeDSL(scope).run {
            scoped { scope }

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
