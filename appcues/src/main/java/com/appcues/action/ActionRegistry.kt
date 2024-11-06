package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.DelayAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.RequestPushAction
import com.appcues.action.appcues.RequestReviewAction
import com.appcues.action.appcues.SubmitFormAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.logging.Logcues
import kotlin.collections.set

internal typealias ActionFactoryBlock = (config: AppcuesConfigMap, renderContext: RenderContext) -> ExperienceAction

internal class ActionRegistry(override val scope: AppcuesScope) : AppcuesComponent {

    private val logcues: Logcues by inject()

    private val actions: MutableMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register(CloseAction.TYPE) { config, context -> CloseAction(config, context, get()) }
        register(ContinueAction.TYPE) { config, context -> ContinueAction(config, context, get()) }
        register(LaunchExperienceAction.TYPE) { config, context -> LaunchExperienceAction(config, context, get()) }
        register(SubmitFormAction.TYPE) { config, context -> SubmitFormAction(config, context, get(), get()) }
        register(LinkAction.TYPE) { config, _ -> LinkAction(config, get(), get(), get()) }
        register(TrackEventAction.TYPE) { config, _ -> TrackEventAction(config, get()) }
        register(UpdateProfileAction.TYPE) { config, _ -> UpdateProfileAction(config, get()) }
        register(RequestReviewAction.TYPE) { config, _ -> RequestReviewAction(config, get(), get()) }
        register(DelayAction.TYPE) { config, _ -> DelayAction(config) }
        register(RequestPushAction.TYPE) { config, _ -> RequestPushAction(config, get(), get()) }
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
