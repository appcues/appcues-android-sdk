package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.di.AppcuesKoinComponent
import com.appcues.logging.Logcues
import com.appcues.trait.appcues.AppcuesBackdropTrait
import com.appcues.trait.appcues.AppcuesModalTrait
import com.appcues.trait.appcues.AppcuesSkippableTrait
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf

internal typealias TraitFactoryBlock = (config: AppcuesConfigMap) -> ExperienceTrait

internal class TraitRegistry(
    override val scopeId: String,
    private val logcues: Logcues
) : AppcuesKoinComponent {

    private val actions: HashMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        register("@appcues/backdrop") { get<AppcuesBackdropTrait> { parametersOf(it) } }
        register("@appcues/modal") { get<AppcuesModalTrait> { parametersOf(it) } }
        register("@appcues/skippable") { get<AppcuesSkippableTrait> { parametersOf(it) } }
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
