package com.appcues.action

import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.logging.Logcues

internal typealias ActionConfigMap = HashMap<String, Any>?

internal typealias ActionFactoryBlock = (config: ActionConfigMap) -> ExperienceAction

internal class ActionRegistry(private val logcues: Logcues) {

    private val actions: HashMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register("@appcues/close") { AppcuesCloseAction(it) }
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
