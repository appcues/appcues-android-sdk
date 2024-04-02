package com.appcues

import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.data.AppcuesRepository
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.logging.LogcatDestination
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.utils.ImageLoaderWrapper
import com.appcues.util.AppcuesViewTreeOwner
import com.appcues.util.LinkOpener

internal object MainModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { Appcues(scope) }
        scoped { AppcuesViewTreeOwner() }
        scoped { TraitRegistry(get(), get()) }
        scoped { ActionRegistry(get()) }
        scoped { ActionProcessor(get()) }
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped { Logcues() }
        scoped { LogcatDestination(get(), get<AppcuesConfig>().loggingLevel) }
        scoped { Storage(context = get(), config = get()) }
        scoped { DeepLinkHandler(scope = scope) }
        scoped { AppcuesDebuggerManager(appcuesViewTreeOwner = get(), contextWrapper = get(), scope = scope) }
        scoped { StateMachineDirectory() }
        scoped { ExperienceRenderer(scope = scope) }
        scoped {
            AppcuesRepository(
                appcuesRemoteSource = get(),
                appcuesLocalSource = get(),
                experienceMapper = get(),
                config = get(),
                dataLogcues = get(),
                storage = get(),
            )
        }
        scoped { LinkOpener(get()) }
        scoped { AnalyticsPublisher(storage = get()) }

        factory { StateMachine(actionProcessor = get(), lifecycleTracker = get(), onEndedExperience = it.next()) }

        scoped { get<AppcuesConfig>().imageLoader ?: get<ImageLoaderWrapper>().build() }
    }
}
