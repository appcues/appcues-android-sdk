package com.appcues

import coil.ImageLoader
import com.appcues.action.ActionProcessor
import com.appcues.action.ActionRegistry
import com.appcues.data.AppcuesRepository
import com.appcues.data.PushRepository
import com.appcues.debugger.AppcuesDebuggerManager
import com.appcues.di.AppcuesModule
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.logging.LogcatDestination
import com.appcues.logging.Logcues
import com.appcues.push.PushDeeplinkHandler
import com.appcues.push.PushOpenedProcessor
import com.appcues.statemachine.StateMachine
import com.appcues.trait.TraitRegistry
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.StateMachineDirectory
import com.appcues.ui.utils.ImageLoaderWrapper
import com.appcues.util.AppcuesViewTreeOwner
import com.appcues.util.DataSanitizer
import com.appcues.util.LinkOpener
import kotlinx.coroutines.CoroutineScope

internal object MainModule : AppcuesModule {

    override fun AppcuesScopeDSL.install() {
        scoped { Appcues(scope) }
        scoped { AppcuesViewTreeOwner() }
        scoped { TraitRegistry(get(), get()) }
        scoped { ActionRegistry(get()) }
        scoped { ActionProcessor(get()) }
        scoped<CoroutineScope> { AppcuesCoroutineScope(logcues = get()) }
        scoped { Logcues() }
        scoped { LogcatDestination(get(), get<AppcuesConfig>().loggingLevel) }
        scoped { Storage(context = get(), config = get()) }
        scoped { DeepLinkHandler(scope = scope) }
        scoped { PushDeeplinkHandler(scope) }
        scoped { PushOpenedProcessor(scope) }
        scoped { AppcuesDebuggerManager(appcuesViewTreeOwner = get(), contextWrapper = get(), scope = scope, appcuesConfig = get()) }
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
        scoped { PushRepository(get(), get()) }
        scoped { LinkOpener(get()) }
        scoped { DataSanitizer() }
        scoped { AnalyticsPublisher(get(), get()) }

        factory { StateMachine(actionProcessor = get(), lifecycleTracker = get(), onEndedExperience = it.next()) }

        scoped<ImageLoader> { get<ImageLoaderWrapper>().build() }
    }
}
