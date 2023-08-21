package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.analytics.track
import com.appcues.analytics.trackExperienceError
import com.appcues.analytics.trackExperienceRecovery
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.RenderContextNotActive
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.SharedFlow
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.scope.Scope

internal class ExperienceRenderer(
    override val scope: Scope,
) : KoinScopeComponent, StateMachineOwning {

    // Conformance to `StateMachineOwning`.
    override var renderContext: RenderContext? = null
    // State machine for `RenderContext.modal`
    override var stateMachine: StateMachine? = null

    // lazy prop inject here to avoid circular dependency with AnalyticsTracker
    // AnalyticsTracker > AnalyticsQueueProcessor > ExperienceRenderer(this) > AnalyticsTracker
    private val repository by inject<AppcuesRepository>()
    private val sessionMonitor by inject<SessionMonitor>()
    private val config by inject<AppcuesConfig>()
    private val analyticsTracker by inject<AnalyticsTracker>()
    private val stateMachines by inject<StateMachineDirectory>()
    private val lifecycleTracker by inject<ExperienceLifecycleTracker>()

    private val potentiallyRenderableExperiences = hashMapOf<RenderContext, List<Experience>>()

    init {
        // sets up the single state machine used for modal experiences (mobile flows, not embeds)
        // that lives indefinitely and handles one experience at a time
        stateMachine = get<StateMachine>().also {
            lifecycleTracker.start(it, { /* no action needed on end of modal experience */ })
        }
        stateMachines.setOwner(Modal, this)
    }

    fun getStateFlow(renderContext: RenderContext): SharedFlow<State>? {
        return stateMachines.getOwner(renderContext)?.stateMachine?.stateFlow
    }

    fun getState(renderContext: RenderContext): State? {
        return stateMachines.getOwner(renderContext)?.stateMachine?.state
    }

    /**
     * returns true/false whether the experience is was started
     */
    suspend fun show(experience: Experience): Boolean = with(experience) {
        var canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true

        // if there is an active experiment, and we should not show this experience (control group), then
        // track the analytics for experiment_entered, but ensure we exit early. This should be checked before
        // we dismiss any current experience below.
        if (experiment?.group == "control") {
            analyticsTracker.track(experiment)
            canShow = false
        }

        if (!canShow) return false

        val stateMachine = stateMachines.getOwner(renderContext)?.stateMachine

        if (stateMachine == null) {
            analyticsTracker.trackExperienceError(experience, "no render context $renderContext")
            return false
        }

        if (stateMachine.checkPriority(this)) {
            return when (stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false))) {
                is Success -> show(this) // re-invoke show on the new experience now after dismiss
                is Failure -> false // dismiss failed - can't continue
            }
        }

        // track an experiment_entered analytic, if exists, since we know it is not in the control group at this point
        experiment?.let { analyticsTracker.track(it) }

        when (stateMachine.handleAction(StartExperience(this))) {
            is Success -> true
            is Failure -> false
        }
    }

    private fun StateMachine.checkPriority(newExperience: Experience): Boolean {
        // "event_trigger" or "forced" experience priority is NORMAL, "screen_view" is low -
        // if an experience is currently showing and the new experience coming in is normal priority
        // then it replaces whatever is currently showing - i.e. an "event_trigger" experience will
        // supersede a "screen_view" triggered experience - per Appcues standard behavior
        return newExperience.priority == NORMAL && state != Idling
    }

    suspend fun show(experiences: List<Experience>) {
        val trigger = experiences.firstOrNull()?.trigger

        if (trigger is Qualification && trigger.reason == "screen_view") {
            // clear list in case this was a screen_view qualification
            potentiallyRenderableExperiences.clear()
            stateMachines.cleanup()
        }

        // Add new experiences, replacing any existing ones
        potentiallyRenderableExperiences.putAll(experiences.groupBy { it.renderContext })

        // make a copy while we try to show them, so anything else editing the
        // main mapping won't hit a concurrent modification exception
        val experiencesToTry = potentiallyRenderableExperiences.toMap()
        experiencesToTry.forEach {
            attemptToShow(it.value)
        }

        // No caching required for modals since they can't be lazy-loaded.
        potentiallyRenderableExperiences.remove(Modal)
    }

    private suspend fun attemptToShow(experiences: List<Experience>) {
        // ensure list is not empty, after that we get the first experience and try to show it.
        // If it does not show we drop that experience and recursively call this again
        if (experiences.isEmpty()) return

        val experience = experiences.first()
        if (!show(experience)) {
            attemptToShow(experiences.drop(1))
        } else {
            analyticsTracker.trackExperienceRecovery(experience)
        }
    }

    suspend fun start(owner: StateMachineOwning, context: RenderContext) {
        // If there's already a frame for the context, reset it back to its unregistered state.
        val existingOwner = stateMachines.getOwner(context) as? AppcuesFrameStateMachineOwner
        if (existingOwner != null) {
            existingOwner.frame.get()?.reset()
            existingOwner.stateMachine = null
        }

        // If the machine being started is already registered for a different context,
        // reset it back to its unregistered state before potentially showing new content.
        if (owner.stateMachine != null) {
            (owner as? AppcuesFrameStateMachineOwner)?.let {
                it.frame.get()?.reset()
                it.stateMachine = null
            }
        }

        owner.stateMachine = get<StateMachine>().also {
            lifecycleTracker.start(it, { potentiallyRenderableExperiences.remove(renderContext) })
        }
        stateMachines.setOwner(context, owner)

        show(context)
    }

    suspend fun show(renderContext: RenderContext) {
        potentiallyRenderableExperiences[renderContext]?.let {
            attemptToShow(it)
        }
    }

    suspend fun show(experienceId: String, trigger: ExperienceTrigger): Boolean {
        if (sessionMonitor.sessionId == null) return false

        repository.getExperienceContent(experienceId, trigger)?.let {
            return show(it)
        }

        return false
    }

    suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        stateMachines.getOwner(renderContext)?.stateMachine?.handleAction(StartStep(stepReference))
    }

    suspend fun preview(experienceId: String): Boolean {
        repository.getExperiencePreview(experienceId)?.let {
            return show(it)
        }

        return false
    }

    fun stop() {
        stateMachines.stop()
    }

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> {
        return stateMachines.getOwner(renderContext)?.stateMachine?.handleAction(EndExperience(markComplete, destroyed))
            ?: Failure(RenderContextNotActive(renderContext))
    }
}
