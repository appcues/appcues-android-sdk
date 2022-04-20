package com.appcues

import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.debugger.AppcuesDebuggerManager
import com.appcues.ui.debugger.DebuggerDataManager
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { config }
        scoped { Appcues(koinScope = this) }
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped { Logcues(config.loggingLevel) }
        scoped { StateMachine(appcuesCoroutineScope = get(), config = get()) }
        scoped { Storage(context = get(), config = get()) }
        scoped { DeeplinkHandler(config = get(), appcues = get(), experienceRenderer = get(), appcuesCoroutineScope = get()) }
        scoped { AppcuesDebuggerManager(context = get(), koinScope = this) }
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
                gson = get(),
                config = get(),
                logcues = get()
            )
        }

        scoped {
            DebuggerDataManager(
                appcuesConfig = get(),
                storage = get(),
                appcuesRemoteSource = get(),
            )
        }
    }
}
