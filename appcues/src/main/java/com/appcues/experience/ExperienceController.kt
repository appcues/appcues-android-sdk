package com.appcues.experience

import com.appcues.Appcues
import com.appcues.action.ActionRegistry
import com.appcues.di.AppcuesKoinComponent
import com.appcues.domain.entity.Experience
import com.appcues.domain.entity.action.Action
import com.appcues.domain.gateway.ExperienceGateway
import com.appcues.experience.modal.ModalStepController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import java.util.UUID
import kotlin.collections.set

internal class ExperienceController(
    override val scopeId: String,
) : ExperienceGateway, AppcuesKoinComponent {

    private val appcues by inject<Appcues>()

    private val actionRegistry by inject<ActionRegistry>()

    private val experiencesMap: HashMap<UUID, Experience> = hashMapOf()

    private val stepControllerMap: HashMap<UUID, StepController> = hashMapOf()

    internal fun getExperience(id: UUID): Experience? {
        return experiencesMap[id]
    }

    suspend fun executeActions(experienceId: UUID, actions: List<Action>) {
        withContext(Dispatchers.IO) {
            val stepController = stepControllerMap[experienceId]
            if (stepController != null) {
                actions.forEach {
                    actionRegistry[it.type]?.execute(appcues, stepController, it.config)
                }
            }
        }
    }

    override suspend fun start(experience: Experience) {
        withContext(Dispatchers.IO) {
            experiencesMap[experience.id] = experience

            stepControllerMap[experience.id] = ModalStepController(scopeId, experience).also {
                it.begin()
            }
        }
    }
}
