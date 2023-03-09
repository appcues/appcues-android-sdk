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
        factory { LinkAction(config = it.getOrNull(), linkOpener = get()) }
        factory { TrackEventAction(config = it.getOrNull()) }
        factory { ContinueAction(config = it.getOrNull(), stateMachine = get()) }
        factory { LaunchExperienceAction(config = it.getOrNull(), stateMachine = get(), experienceRenderer = get()) }
        factory { UpdateProfileAction(config = it.getOrNull(), storage = get()) }
        factory { SubmitFormAction(config = it.getOrNull(), analyticsTracker = get(), stateMachine = get()) }
        factory { StepInteractionAction(config = it.getOrNull(), interaction = it.get(), analyticsTracker = get(), stateMachine = get()) }
        factory { RequestReviewAction(config = it.getOrNull(), context = get(), koinScope = get()) }
    }
}
