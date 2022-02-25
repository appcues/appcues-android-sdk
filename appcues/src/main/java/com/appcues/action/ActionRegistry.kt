package com.appcues.action

import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.action.appcues.AppcuesLinkAction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.di.AppcuesKoinComponent
import com.appcues.logging.Logcues
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

typealias ActionFactoryBlock = (config: AppcuesConfigMap) -> ExperienceAction

internal class ActionRegistry(
    override val scopeId: String,
    private val logcues: Logcues
) : AppcuesKoinComponent {

    private val actions: HashMap<String, ActionFactoryBlock> = hashMapOf()

    init {
        register("@appcues/close") { get<AppcuesCloseAction> { parametersOf(it) } }
        register("@appcues/link") { get<AppcuesLinkAction> { parametersOf(it) } }
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
