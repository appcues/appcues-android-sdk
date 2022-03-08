package com.appcues.action

import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.LinkAction
import com.appcues.action.appcues.TrackEventAction
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

    private val actions: HashMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register("@appcues/close") { get<CloseAction> { parametersOf(it) } }
        register("@appcues/link") { get<LinkAction> { parametersOf(it) } }
        register("@appcues/track") { get<TrackEventAction> { parametersOf(it) } }
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
