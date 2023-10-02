package com.appcues

import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.data.AppcuesRepository
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.utils.ImageLoaderWrapper
import com.appcues.util.LinkOpener

internal object MainModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { Appcues(scope) }
        scoped { TraitRegistry(get(), get()) }
        scoped { ActionRegistry(get()) }
        scoped { ActionProcessor(get()) }
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped {
            val config: AppcuesConfig = get()
            Logcues(config.loggingLevel)
        }
        scoped { Storage(context = get(), config = get()) }
        scoped {
            DeepLinkHandler(
                config = get(),
                experienceRenderer = get(),
                appcuesCoroutineScope = get(),
                debuggerManager = get(),
            )
        }
        scoped { AppcuesDebuggerManager(contextWrapper = get(), scope = scope) }
        scoped { StateMachineDirectory() }
        scoped { ExperienceRenderer(scope = scope) }
        scoped {
            AppcuesRepository(
                appcuesRemoteSource = get(),
                appcuesLocalSource = get(),
                experienceMapper = get(),
                config = get(),
                logcues = get(),
                storage = get(),
            )
        }
        scoped { LinkOpener(get()) }
        scoped { AnalyticsPublisher(storage = get()) }

        factory {
            StateMachine(
                appcuesCoroutineScope = get(),
                config = get(),
                actionProcessor = get(),
                lifecycleTracker = get()
            )
        }

        scoped { get<AppcuesConfig>().imageLoader ?: get<ImageLoaderWrapper>().build() }
    }
}
