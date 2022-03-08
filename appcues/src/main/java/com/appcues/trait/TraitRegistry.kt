package com.appcues.trait

import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues
import com.appcues.trait.appcues.BackdropTrait
import com.appcues.trait.appcues.ModalTrait
import com.appcues.trait.appcues.SkippableTrait
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.scope.Scope
import kotlin.collections.set

internal typealias TraitFactoryBlock = (config: AppcuesConfigMap) -> ExperienceTrait

internal class TraitRegistry(
    override val scope: Scope,
    private val logcues: Logcues
) : KoinScopeComponent {

    private val actions: HashMap<String, TraitFactoryBlock> = hashMapOf()

    init {
        register("@appcues/backdrop") { get<BackdropTrait> { parametersOf(it) } }
        register("@appcues/modal") { get<ModalTrait> { parametersOf(it) } }
        register("@appcues/skippable") { get<SkippableTrait> { parametersOf(it) } }
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
