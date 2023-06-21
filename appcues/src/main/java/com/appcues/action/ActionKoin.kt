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
        scoped { ActionRegistry(get()) }
        scoped { ActionProcessor(get()) }

        // access to internal in context actions
        factory { CloseAction(it.getOrNull(), it.get(), get()) }
        factory { ContinueAction(it.getOrNull(), it.get(), get()) }
        factory { LaunchExperienceAction(it.getOrNull(), it.get(), get()) }
        factory { StepInteractionAction(it.getOrNull(), it.get(), it.get(), get(), get()) }
        factory { SubmitFormAction(it.getOrNull(), it.get(), get(), get()) }

        // other
        factory { LinkAction(it.getOrNull(), get(), get()) }
        factory { TrackEventAction(it.getOrNull(), get()) }
        factory { UpdateProfileAction(it.getOrNull(), get(), get()) }
        factory { RequestReviewAction(it.getOrNull(), get(), get()) }
    }
}
