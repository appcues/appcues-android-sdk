package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.analytics.track
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
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
) : KoinScopeComponent {

    // lazy prop inject here to avoid circular dependency with AnalyticsTracker
    // AnalyticsTracker > AnalyticsQueueProcessor > ExperienceRenderer(this) > AnalyticsTracker
    private val repository by inject<AppcuesRepository>()
    private val sessionMonitor by inject<SessionMonitor>()
    private val config by inject<AppcuesConfig>()
    private val analyticsTracker by inject<AnalyticsTracker>()
    private val experienceTracker by inject<ExperienceLifecycleTracker>()

    private val slots: HashMap<RenderContext, StateMachine> = hashMapOf()

    fun getStateFlow(renderContext: RenderContext): SharedFlow<State>? {
        return slots[renderContext]?.stateFlow
    }

    fun getState(renderContext: RenderContext): State? {
        return slots[renderContext]?.state
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

        val stateMachine = slots.getOrCreateStateMachine(renderContext)

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

    private fun HashMap<RenderContext, StateMachine>.getOrCreateStateMachine(renderContext: RenderContext): StateMachine {
        return get(renderContext) ?: run {
            get<StateMachine>()
                .also { put(renderContext, it) }
                .also { experienceTracker.start(it) }
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
        showEmbeds(experiences.filter { it.renderContext is RenderContext.Embed })
        showModal(experiences.filter { it.renderContext is RenderContext.Modal })
    }

    private suspend fun showModal(experiences: List<Experience>) {
        // ensure list is not empty, after that we get the first experience and try to show it.
        // If it does not show we drop that experience and recursively call this again
        if (experiences.isEmpty()) return

        if (!show(experiences.first())) {
            showModal(experiences.drop(1))
        }
    }

    private suspend fun showEmbeds(experiences: List<Experience>) {
        experiences.forEach {
            // TODO what to do hereª
            show(it)
        }
    }

    suspend fun show(experienceId: String, trigger: ExperienceTrigger): Boolean {
        if (!sessionMonitor.checkSession("cannot show Experience $experienceId")) return false

        repository.getExperienceContent(experienceId, trigger)?.let {
            return show(it)
        }

        return false
    }

    suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        slots[renderContext]?.handleAction(StartStep(stepReference))
    }

    suspend fun preview(experienceId: String): Boolean {
        repository.getExperiencePreview(experienceId)?.let {
            return show(it)
        }

        return false
    }

    fun stop() {
        slots.values.forEach { it.stop() }
    }

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> {
        return slots[renderContext]?.handleAction(EndExperience(markComplete, destroyed)) ?: Failure(RenderContextNotActive(renderContext))
    }
}
