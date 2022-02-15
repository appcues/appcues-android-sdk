package com.appcues.action

import com.appcues.action.appcues.AppcuesCloseAction
import com.appcues.di.AppcuesKoinComponent

internal class ActionRegistry(override val scopeId: String) : AppcuesKoinComponent {

    private val actions: HashMap<String, ExperienceAction> = hashMapOf()

    init {
        register(AppcuesCloseAction())
    }

    fun register(experienceAction: ExperienceAction) {
        actions[experienceAction.type] = experienceAction
    }

    operator fun get(key: String) = actions[key]
}
