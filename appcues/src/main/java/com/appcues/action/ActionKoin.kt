package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object ActionKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { ActionRegistry(scope = get(), logcues = get()) }
        scoped { ActionProcessor(appcues = get(), appcuesCoroutineScope = get()) }

        factory { params ->
            CloseAction(
                config = params.getOrNull(),
                stateMachine = get(),
            )
        }

        factory { params ->
            LinkAction(
                config = params.getOrNull(),
                linkOpener = get(),
            )
        }

        factory { params ->
            TrackEventAction(
                config = params.getOrNull()
            )
        }

        factory { params ->
            ContinueAction(
                config = params.getOrNull(),
                stateMachine = get(),
            )
        }

        factory { params ->
            LaunchExperienceAction(
                config = params.getOrNull(),
            )
        }

        factory { params ->
            UpdateProfileAction(
                config = params.getOrNull(),
                storage = get(),
            )
        }
    }
}
