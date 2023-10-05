package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.AppcuesFrameView
import com.appcues.SessionMonitor
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.track
import com.appcues.analytics.trackExperienceRecovery
import com.appcues.analytics.trackRecoverableExperienceError
import com.appcues.data.AppcuesRepository
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.QualificationResult
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.data.model.StepReference
import com.appcues.data.model.getFrameId
import com.appcues.data.remote.RemoteError.HttpError
import com.appcues.di.component.AppcuesComponent
import com.appcues.di.component.get
import com.appcues.di.component.inject
import com.appcues.di.scope.AppcuesScope
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.MoveToStep
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Error
import com.appcues.statemachine.State
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.IdlingState
import com.appcues.ui.ExperienceRenderer.PreviewResponse.Failed
import com.appcues.ui.ExperienceRenderer.PreviewResponse.PreviewDeferred
import com.appcues.ui.ExperienceRenderer.RenderingResult.NoRenderContext
import com.appcues.ui.ExperienceRenderer.RenderingResult.StateMachineError
import com.appcues.ui.ExperienceRenderer.RenderingResult.WontDisplay
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.flow.SharedFlow

internal class ExperienceRenderer(override val scope: AppcuesScope) : AppcuesComponent {

    companion object {

        private const val HTTP_CODE_NOT_FOUND = 404
    }

    // lazy prop inject here to avoid circular dependency with AnalyticsTracker
    // AnalyticsTracker > AnalyticsQueueProcessor > ExperienceRenderer(this) > AnalyticsTracker
    private val repository by inject<AppcuesRepository>()
    private val sessionMonitor by inject<SessionMonitor>()
    private val config by inject<AppcuesConfig>()
    private val analyticsTracker by inject<AnalyticsTracker>()
    private val stateMachines by inject<StateMachineDirectory>()

    private val potentiallyRenderableExperiences = hashMapOf<RenderContext, List<Experience>>()
    private val previewExperiences = hashMapOf<RenderContext, Experience>()

    private fun onExperienceEnded(experience: Experience) {
        // when an experience completes, remove this render context from the cache, until a new set of
        // qualified experiences is processed
        potentiallyRenderableExperiences.remove(experience.renderContext)
    }

    init {
        // sets up the single state machine used for modal experiences (mobile flows, not embeds)
        // that lives indefinitely and handles one experience at a time
        stateMachines.setOwner(ModalStateMachineOwner(get(::onExperienceEnded)))
    }

    fun getStateFlow(renderContext: RenderContext): SharedFlow<State>? {
        return stateMachines.getOwner(renderContext)?.stateMachine?.stateFlow
    }

    fun getState(renderContext: RenderContext): State? {
        return stateMachines.getOwner(renderContext)?.stateMachine?.state
    }

    internal sealed class RenderingResult {
        object Success : RenderingResult()
        object WontDisplay : RenderingResult()
        data class NoRenderContext(val experience: Experience, val renderContext: RenderContext) : RenderingResult()
        data class StateMachineError(val experience: Experience, val error: Error) : RenderingResult()
    }

    /**
     * returns true/false whether the experience is was started
     */
    suspend fun show(experience: Experience): RenderingResult = with(experience) {
        var canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true

        // if there is an active experiment, and we should not show this experience (control group), then
        // track the analytics for experiment_entered, but ensure we exit early. This should be checked before
        // we dismiss any current experience below.
        if (experiment?.group == "control") {
            analyticsTracker.track(experiment)
            canShow = false
        }

        if (!canShow) return WontDisplay

        val stateMachine = stateMachines.getOwner(renderContext)?.stateMachine ?: return NoRenderContext(experience, renderContext)
            .also { analyticsTracker.trackRecoverableExperienceError(experience, "no render context $renderContext") }

        if (stateMachine.checkPriority(this)) {
            return when (val result = stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false))) {
                is Success -> show(this)
                is Failure -> StateMachineError(experience, result.reason)
            }
        }

        // track an experiment_entered analytic, if exists, since we know it is not in the control group at this point
        experiment?.let { analyticsTracker.track(it) }

        return when (val result = stateMachine.handleAction(StartExperience(this))) {
            is Success -> RenderingResult.Success.also { previewExperiences.remove(renderContext) }
            is Failure -> StateMachineError(experience, result.reason)
        }
    }

    private fun StateMachine.checkPriority(newExperience: Experience): Boolean {
        // "event_trigger" or "forced" experience priority is NORMAL, "screen_view" is low -
        // if an experience is currently showing and the new experience coming in is normal priority
        // then it replaces whatever is currently showing - i.e. an "event_trigger" experience will
        // supersede a "screen_view" triggered experience - per Appcues standard behavior
        return newExperience.priority == NORMAL && state !is IdlingState
    }

    suspend fun show(qualificationResult: QualificationResult) {
        if (qualificationResult.trigger.reason == "screen_view") {
            // clear list in case this was a screen_view qualification
            potentiallyRenderableExperiences.clear()
            stateMachines.cleanup()
        }

        // Add new experiences, replacing any existing ones
        potentiallyRenderableExperiences.putAll(qualificationResult.experiences.groupBy { it.renderContext })

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
        if (show(experience) != RenderingResult.Success) {
            attemptToShow(experiences.drop(1))
        } else {
            analyticsTracker.trackExperienceRecovery(experience)
        }
    }

    suspend fun start(frame: AppcuesFrameView, context: RenderContext) {
        // If there's already a frame for the context, reset it back to its unregistered state.
        stateMachines.getOwner(context)?.reset()

        // if this frame is already registered for a different context, reset it back - a frame
        // can only be registered to a single RenderContext
        stateMachines.getOwner(frame)?.let {
            if (it.renderContext != context) {
                it.reset()
            }
        }

        val stateMachine: StateMachine = get(::onExperienceEnded)

        val owner = AppcuesFrameStateMachineOwner(frame, context, stateMachine)

        stateMachines.setOwner(owner)

        show(context)
    }

    suspend fun show(renderContext: RenderContext) {
        // shows preview if exist or else show possible experiences from qualification
        previewExperiences[renderContext]?.let { show(it) }
            ?: potentiallyRenderableExperiences[renderContext]?.let { attemptToShow(it) }
    }

    suspend fun show(experienceId: String, trigger: ExperienceTrigger): Boolean {
        if (sessionMonitor.sessionId == null) return false

        repository.getExperienceContent(experienceId, trigger)?.let {
            return when (show(it)) {
                RenderingResult.Success -> true
                else -> false
            }
        }

        return false
    }

    suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        stateMachines.getOwner(renderContext)?.stateMachine?.handleAction(MoveToStep(stepReference))
    }

    sealed class PreviewResponse {
        object Success : PreviewResponse()
        object ExperienceNotFound : PreviewResponse()
        object Failed : PreviewResponse()
        data class PreviewDeferred(val experience: Experience, val frameId: String?) : PreviewResponse()
        data class StateMachineError(val experience: Experience, val error: Error) : PreviewResponse()
    }

    suspend fun preview(experienceId: String): PreviewResponse {
        repository.getExperiencePreview(experienceId).run {
            return when (this) {
                is Success -> {
                    previewExperiences[value.renderContext] = value

                    return when (val result = show(value)) {
                        is NoRenderContext -> PreviewDeferred(result.experience, result.renderContext.getFrameId())
                        is StateMachineError -> PreviewResponse.StateMachineError(result.experience, result.error)
                        else -> PreviewResponse.Success
                    }
                }
                is Failure -> if (reason is HttpError && reason.code == HTTP_CODE_NOT_FOUND)
                    PreviewResponse.ExperienceNotFound else Failed
            }
        }
    }

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean) {
        stateMachines.getOwner(renderContext)?.stateMachine?.handleAction(EndExperience(markComplete, destroyed))
    }

    suspend fun resetAll() {
        previewExperiences.clear()
        potentiallyRenderableExperiences.clear()
        stateMachines.resetAll()
    }
}
