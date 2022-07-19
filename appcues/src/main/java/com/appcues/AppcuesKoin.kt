package com.appcues

import com.appcues.data.AppcuesRepository
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
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
        scoped { StateMachine(appcuesCoroutineScope = get(), config = get(), actionProcessor = get()) }
        scoped { Storage(context = get(), config = get()) }
        scoped {
            DeeplinkHandler(
                config = get(),
                experienceRenderer = get(),
                appcuesCoroutineScope = get(),
                debuggerManager = get(),
            )
        }
        scoped { AppcuesDebuggerManager(context = get(), koinScope = this) }
        scoped { ContextResources(context = get()) }
        scoped {
            ExperienceRenderer(
                repository = get(),
                stateMachine = get(),
                sessionMonitor = get(),
                config = get(),
            )
        }
        scoped {
            AppcuesRepository(
                appcuesRemoteSource = get(),
                appcuesLocalSource = get(),
                experienceMapper = get(),
                config = get(),
                logcues = get()
            )
        }
        scoped { LinkOpener(get()) }
    }
}
