package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.RequestReviewAction
import com.appcues.action.appcues.SubmitFormAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope

internal fun Scope.closeActionFactory(): ActionFactoryBlock {
    return { config, renderContext -> get<CloseAction> { parametersOf(config, renderContext) } }
}

internal fun Scope.continueActionFactory(): ActionFactoryBlock {
    return { config, renderContext -> get<ContinueAction> { parametersOf(config, renderContext) } }
}

internal fun Scope.launchExperienceActionFactory(): ActionFactoryBlock {
    return { config, renderContext -> get<LaunchExperienceAction> { parametersOf(config, renderContext) } }
}

internal fun Scope.submitFormActionFactory(): ActionFactoryBlock {
    return { config, renderContext -> get<SubmitFormAction> { parametersOf(config, renderContext) } }
}

internal fun Scope.linkActionFactory(): ActionFactoryBlock {
    return { config, _ -> get<LinkAction> { parametersOf(config) } }
}

internal fun Scope.trackEventActionFactory(): ActionFactoryBlock {
    return { config, _ -> get<TrackEventAction> { parametersOf(config) } }
}

internal fun Scope.updateProfileActionFactory(): ActionFactoryBlock {
    return { config, _ -> get<UpdateProfileAction> { parametersOf(config) } }
}

internal fun Scope.requestReviewActionFactory(): ActionFactoryBlock {
    return { config, _ -> get<RequestReviewAction> { parametersOf(config) } }
}
