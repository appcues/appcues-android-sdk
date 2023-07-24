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
        factory { CloseAction(it[0], it[1], get()) }
        factory { ContinueAction(it[0], it[1], get()) }
        factory { LaunchExperienceAction(it[0], it[1], get()) }
        factory { StepInteractionAction(it[0], it[1], get(), get()) }
        factory { SubmitFormAction(it[0], it[1], get(), get()) }

        // other
        factory { LinkAction(config = it[0], get(), get()) }
        factory { TrackEventAction(it[0], get()) }
        factory { UpdateProfileAction(it[0], get(), get()) }
        factory { RequestReviewAction(it[0], get(), get()) }
    }
}
