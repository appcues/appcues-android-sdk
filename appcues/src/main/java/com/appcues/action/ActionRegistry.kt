package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.RequestReviewAction
import com.appcues.action.appcues.SubmitFormAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.logging.Logcues
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import kotlin.collections.set

internal typealias ActionFactoryBlock = (
    config: AppcuesConfigMap,
    renderContext: RenderContext
) -> ExperienceAction

internal class ActionRegistry(override val scope: Scope) : KoinScopeComponent {

    private val logcues: Logcues by inject()

    private val actions: MutableMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register(CloseAction.TYPE, scope.closeActionFactory())
        register(ContinueAction.TYPE, scope.continueActionFactory())
        register(LaunchExperienceAction.TYPE, scope.launchExperienceActionFactory())
        register(SubmitFormAction.TYPE, scope.submitFormActionFactory())
        register(LinkAction.TYPE, scope.linkActionFactory())
        register(TrackEventAction.TYPE, scope.trackEventActionFactory())
        register(UpdateProfileAction.TYPE, scope.updateProfileActionFactory())
        register(RequestReviewAction.TYPE, scope.requestReviewActionFactory())
    }

    operator fun get(key: String): ActionFactoryBlock? {
        return actions[key]
    }

    fun register(type: String, factory: (config: Map<String, Any>?) -> ExperienceAction) {
        register(type) { config, _ -> factory(config) }
    }

    private fun register(type: String, factory: ActionFactoryBlock) {
        if (actions.contains(type)) {
            logcues.error(AppcuesDuplicateActionException(type))
        } else {
            actions[type] = factory
        }
    }

    private class AppcuesDuplicateActionException(type: String) :
        Exception("Fail to register action $type: Action already registered")
}
