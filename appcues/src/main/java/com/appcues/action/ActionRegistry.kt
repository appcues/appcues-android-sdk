package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.collections.set

typealias ActionFactoryBlock = (config: AppcuesConfigMap) -> ExperienceAction

internal class ActionRegistry(
    override val scope: Scope,
    private val logcues: Logcues
) : KoinScopeComponent {

    private val actions: MutableMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register(CloseAction.TYPE) { get<CloseAction> { parametersOf(it) } }
        register(LinkAction.TYPE) { get<LinkAction> { parametersOf(it) } }
        register(TrackEventAction.TYPE) { get<TrackEventAction> { parametersOf(it) } }
        register(ContinueAction.TYPE) { get<ContinueAction> { parametersOf(it) } }
        register(UpdateProfileAction.TYPE) { get<UpdateProfileAction> { parametersOf(it) } }
        register(LaunchExperienceAction.TYPE) { get<LaunchExperienceAction> { parametersOf(it) } }
    }

    operator fun get(key: String): ActionFactoryBlock? {
        return actions[key]
    }

    fun register(type: String, factory: ActionFactoryBlock) {
        if (actions.contains(type)) {
            logcues.error(AppcuesDuplicateActionException(type))
        } else {
            actions[type] = factory
        }
    }

    private class AppcuesDuplicateActionException(type: String) :
        Exception("Fail to register action $type: Action already registered")
}
