package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.RequestReviewAction
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.action.appcues.SubmitFormAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.di.KoinScopePlugin
import org.koin.dsl.ScopeDSL

internal object ActionKoin : KoinScopePlugin {

    override fun ScopeDSL.install() {
        scoped { ActionRegistry(scope = get()) }
        scoped { ActionProcessor(scope = get()) }

        factory { CloseAction(config = it.getOrNull(), experienceRenderer = get()) }
        factory { LinkAction(config = it.getOrNull(), linkOpener = get(), appcues = get()) }
        factory { TrackEventAction(config = it.getOrNull(), appcues = get()) }
        factory { ContinueAction(config = it.getOrNull(), experienceRenderer = get()) }
        factory { LaunchExperienceAction(config = it.getOrNull(), experienceRenderer = get()) }
        factory { UpdateProfileAction(config = it.getOrNull(), storage = get(), appcues = get()) }
        factory { SubmitFormAction(config = it.getOrNull(), analyticsTracker = get(), experienceRenderer = get()) }
        factory {
            StepInteractionAction(
                config = it.getOrNull(),
                interaction = it.get(),
                analyticsTracker = get(),
                experienceRenderer = get()
            )
        }
        factory { RequestReviewAction(config = it.getOrNull(), context = get(), koinScope = get()) }
    }
}
