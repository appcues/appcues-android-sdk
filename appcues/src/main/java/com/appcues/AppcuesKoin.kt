package com.appcues

import com.appcues.data.AppcuesRepository
import com.appcues.di.KoinScopePlugin
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import org.koin.dsl.ScopeDSL

internal object AppcuesKoin : KoinScopePlugin {

    override fun ScopeDSL.install(config: AppcuesConfig) {
        scoped { config }
        scoped { Appcues(koinScope = this) }
        scoped { AppcuesCoroutineScope(logcues = get()) }
        scoped { Logcues(config.loggingLevel) }
        scoped { StateMachine(appcuesCoroutineScope = get(), storage = get()) }
        scoped { Storage(context = get(), config = get()) }
        scoped { ExperienceRenderer(appcuesCoroutineScope = get(), repository = get(), stateMachine = get()) }
        scoped { AppcuesRepository(appcuesRemoteSource = get(), appcuesLocalSource = get(), experienceMapper = get(), gson = get()) }
    }
}
