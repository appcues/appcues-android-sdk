package com.appcues.rendering

import com.appcues.AppcuesConfig
import com.appcues.analytics.RenderingService
import com.appcues.analytics.RenderingService.EventTracker
import com.appcues.analytics.track
import com.appcues.analytics.trackExperienceError
import com.appcues.analytics.trackExperienceRecovery
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.data.model.getFrameId
import com.appcues.rendering.ExperienceRendering.PreviewResponse.PreviewDeferred
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error
import com.appcues.statemachine.Error.RenderContextNotActive
import com.appcues.statemachine.State
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateMachineFactory
import com.appcues.statemachine.StepReference
import com.appcues.ui.ExperienceRenderer.RenderingResult
import com.appcues.ui.ExperienceRenderer.RenderingResult.NoRenderContext
import com.appcues.ui.ExperienceRenderer.RenderingResult.StateMachineError
import com.appcues.ui.ExperienceRenderer.RenderingResult.WontDisplay
import com.appcues.ui.ModalStateMachineOwner
import com.appcues.ui.StateMachineDirectory
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success

internal class ExperienceRendering(
    private val config: AppcuesConfig,
    private val stateMachineDirectory: StateMachineDirectory,
    stateMachineFactory: StateMachineFactory,
) : RenderingService {

    private val qualifiedExperiences = hashMapOf<RenderContext, List<Experience>>()
    private val previewExperiences = hashMapOf<RenderContext, Experience>()

    private lateinit var eventTracker: EventTracker

    init {
        // sets up the single state machine used for modal experiences (mobile flows, not embeds)
        // that lives indefinitely and handles one experience at a time
        stateMachineDirectory.setOwner(ModalStateMachineOwner(stateMachineFactory.create()))
    }

    override fun setEventTracker(eventTracker: EventTracker) {
        this.eventTracker = eventTracker
    }

    override suspend fun show(experiences: List<Experience>, clearCache: Boolean) {
        if (clearCache) {
            // clear list in case this was a screen_view qualification
            qualifiedExperiences.clear()
            stateMachineDirectory.cleanup()
        }

        // Add new experiences, replacing any existing ones
        qualifiedExperiences.putAll(experiences.groupBy { it.renderContext })

        // make a copy while we try to show them, so anything else editing the
        // main mapping won't hit a concurrent modification exception
        val experiencesToTry = qualifiedExperiences.toMap()
        experiencesToTry.forEach {
            attemptToShow(it.value)
        }

        // No caching required for modals since they can't be lazy-loaded.
        qualifiedExperiences.remove(Modal)
    }

    private suspend fun attemptToShow(experiences: List<Experience>) {
        // ensure list is not empty, after that we get the first experience and try to show it.
        // If it does not show we drop that experience and recursively call this again
        if (experiences.isEmpty()) return

        val experience = experiences.first()
        if (show(experience) != RenderingResult.Success) {
            attemptToShow(experiences.drop(1))
        } else {
            eventTracker.trackExperienceRecovery(experience)
        }
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
            eventTracker.track(experiment)
            canShow = false
        }

        if (!canShow) return WontDisplay

        val stateMachine = stateMachineDirectory.getOwner(renderContext)?.stateMachine
            ?: return NoRenderContext(experience, renderContext).also {
                eventTracker.trackExperienceError(experience, "no render context $renderContext")
            }

        if (stateMachine.checkPriority(this)) {
            return when (val result = stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false))) {
                is Success -> show(this)
                is Failure -> StateMachineError(experience, result.reason)
            }
        }

        // track an experiment_entered analytic, if exists, since we know it is not in the control group at this point
        experiment?.let { eventTracker.track(it) }

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
        return newExperience.priority == NORMAL && state != Idling
    }

    suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        stateMachineDirectory.getOwner(renderContext)?.stateMachine?.handleAction(StartStep(stepReference))
    }

    sealed class PreviewResponse {
        object Success : PreviewResponse()
        object ExperienceNotFound : PreviewResponse()
        object Failed : PreviewResponse()
        data class PreviewDeferred(val experience: Experience, val frameId: String?) : PreviewResponse()
        data class StateMachineError(val experience: Experience, val error: Error) : PreviewResponse()
    }

    suspend fun preview(experience: Experience): PreviewResponse {
        previewExperiences[experience.renderContext] = experience

        return when (val result = show(experience)) {
            is NoRenderContext -> PreviewDeferred(result.experience, result.renderContext.getFrameId())
            is StateMachineError -> PreviewResponse.StateMachineError(result.experience, result.error)
            else -> PreviewResponse.Success
        }
    }

    suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean): ResultOf<State, Error> {
        return stateMachineDirectory.getOwner(renderContext)?.stateMachine?.handleAction(EndExperience(markComplete, destroyed))
            ?: Failure(RenderContextNotActive(renderContext))
    }

    override suspend fun reset() {
        previewExperiences.clear()
        qualifiedExperiences.clear()
        stateMachineDirectory.resetAll()
    }
}
