package com.appcues.ui

import com.appcues.AppcuesConfig
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
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.NoActiveStateMachine
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateMachine.StateMachineListener
import com.appcues.statemachine.StateMachineFactory
import com.appcues.statemachine.StepReference
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.collections.set

internal class ExperienceRenderer(
    private val config: AppcuesConfig,
    private val experienceLifecycleTracker: ExperienceLifecycleTracker,
    private val repository: AppcuesRepository,
    private val sessionMonitor: SessionMonitor,
    private val stateMachineFactory: StateMachineFactory,
) : StateMachineListener {

    private val stateMachines: HashMap<RenderContext, StateMachine> = hashMapOf()

    private val _stateFlow = MutableSharedFlow<State>(1)
    val stateFlow: SharedFlow<State>
        get() = _stateFlow

    private val _errorFlow = MutableSharedFlow<Error>(1)
    val errorFlow: SharedFlow<Error>
        get() = _errorFlow

    override suspend fun onState(state: State) {
        experienceLifecycleTracker.onState(state)

        _stateFlow.emit(state)
    }

    override suspend fun onError(error: Error) {
        experienceLifecycleTracker.onError(error)

        _errorFlow.emit(error)
    }

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
            val hasActiveStateMachine = it.state != Idling
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
        val stateMachine = stateMachineFactory.create(renderContext, this@ExperienceRenderer)
        return stateMachine.handleAction(StartExperience(this)).let { result ->
            when (result) {
                is Success -> {
                    stateMachines[renderContext] = stateMachine
                    true
                }
                is Failure -> false
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

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> =
        stateMachines[renderContext]?.dismiss(markComplete, destroyed) ?: Failure(NoActiveStateMachine(renderContext))

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
