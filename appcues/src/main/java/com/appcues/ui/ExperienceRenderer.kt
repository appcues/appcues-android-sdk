package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.SessionMonitor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateMachineFactory
import com.appcues.statemachine.StepReference
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.collections.set

internal class ExperienceRenderer(
    private val config: AppcuesConfig,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
    private val experienceLifecycleTracker: ExperienceLifecycleTracker,
    private val repository: AppcuesRepository,
    private val sessionMonitor: SessionMonitor,
    private val stateMachineFactory: StateMachineFactory,
) {

    private val stateMachines: HashMap<RenderContext, StateMachine> = hashMapOf()

    private val _allStates = MutableSharedFlow<State>(1)

    suspend fun collectState(renderContext: RenderContext, action: suspend (value: State) -> Unit) =
        _allStates.filter { it.experience.renderContext == renderContext }.collectLatest(action)

    private val _allErrors = MutableSharedFlow<Error>(1)

    fun getState(renderContext: RenderContext): State? {
        return stateMachines[renderContext]?.state
    }

    fun process(renderContext: RenderContext, actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        stateMachines[renderContext]?.process(actions, interactionType, viewDescription)
    }

    suspend fun show(experience: Experience): Boolean {
        with(experience) {
            if (shouldDisplay().not()) return false

            if (hasConflictingExperience()) return false

            return start()
        }
    }

    private suspend fun Experience.hasConflictingExperience(): Boolean {
        stateMachines[renderContext]?.let {
            val hasActiveStateMachine = it.state !is Idling
            val isHighPriority = priority == NORMAL

            if (hasActiveStateMachine && isHighPriority) {
                return when (it.dismiss(markComplete = false, destroyed = false)) {
                    is Success -> false
                    is Failure -> true
                }
            }
        }

        return false
    }

    private suspend fun Experience.start(): Boolean {
        val stateMachine = stateMachineFactory.create(this)
            .apply { setupFlows() }

        return stateMachine.start().let { result ->
            when (result) {
                is Success -> {
                    stateMachines[renderContext] = stateMachine
                    true
                }
                is Failure -> false
            }
        }
    }

    private fun StateMachine.setupFlows() {
        appcuesCoroutineScope.launch(Dispatchers.IO) {
            stateFlow.collectLatest {
                experienceLifecycleTracker.onState(it)

                _allStates.emit(it)
            }
        }

        appcuesCoroutineScope.launch(Dispatchers.IO) {
            errorFlow.collectLatest {
                experienceLifecycleTracker.onError(it)

                _allErrors.emit(it)
            }
        }
    }

    suspend fun show(experienceId: String, trigger: ExperienceTrigger): Boolean {
        if (!sessionMonitor.checkSession("cannot show Experience $experienceId")) return false

        repository.getExperienceContent(experienceId, trigger)?.let {
            return show(it)
        }

        return false
    }

    suspend fun preview(experienceId: String): Boolean {
        repository.getExperiencePreview(experienceId)?.let {
            return show(it)
        }

        return false
    }

    suspend fun stop() {
        stateMachines.forEach {
            it.value.dismiss(markComplete = false, destroyed = true)
        }
    }

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean) {
        stateMachines[renderContext]?.dismiss(markComplete, destroyed)
    }

    private suspend fun StateMachine.dismiss(markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> {
        return handleAction(EndExperience(markComplete, destroyed))
    }

    suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        stateMachines[renderContext]?.handleAction(StartStep(stepReference))
    }

    private suspend fun Experience.shouldDisplay(): Boolean {
        val canDisplayInterceptor = config.interceptor?.canDisplayExperience(id) ?: true
        return if (experiment == null) canDisplayInterceptor else experiment.group != "control"
    }
}
