package com.appcues

import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachineFactory
import com.appcues.ui.StateMachineDirectory
import com.appcues.util.ContextResources
import com.appcues.util.LinkOpener
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped {
            val config: AppcuesConfig = get()
            Logcues(config.loggingLevel)
        }
        scoped {
            DeepLinkHandler(
                config = get(),
                experiences = get(),
                appcuesCoroutineScope = get(),
                debuggerManager = get(),
            )
        }
        scoped { AppcuesDebuggerManager(context = get(), koinScope = this) }
        scoped { StateMachineDirectory() }
        scoped { ContextResources(context = get()) }
        scoped { LinkOpener(get()) }
        scoped { StateMachineFactory(get(), get(), get()) }
    }
}
