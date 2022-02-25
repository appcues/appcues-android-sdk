package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues

internal typealias TraitFactoryBlock = (config: AppcuesConfigMap) -> ExperienceTrait

internal class TraitRegistry(private val logcues: Logcues) {

    private val actions: HashMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        // register traits
    }

    operator fun get(key: String): TraitFactoryBlock? {
        return actions[key]
    }

    fun register(type: String, factory: TraitFactoryBlock) {
        if (actions.contains(type)) {
            logcues.error(AppcuesDuplicateTraitException(type))
        } else {
            actions[type] = factory
        }
    }

    private class AppcuesDuplicateTraitException(type: String) :
        Exception("Fail to register trait $type: Trait already registered")
}
