package com.appcues.rendering

import com.appcues.AppcuesConfig
import com.appcues.AppcuesFrameView
import com.appcues.analytics.EventTracker
import com.appcues.analytics.RenderingService
import com.appcues.analytics.RenderingService.PreviewExperienceResult
import com.appcues.analytics.RenderingService.ShowExperienceResult
import com.appcues.analytics.track
import com.appcues.analytics.trackExperienceError
import com.appcues.analytics.trackExperienceRecovery
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceState
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Modal
import com.appcues.data.model.StepReference
import com.appcues.data.model.getFrameId
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StateMachineFactory
import com.appcues.ui.AppcuesFrameStateMachineOwner
import com.appcues.ui.ModalStateMachineOwner
import com.appcues.ui.StateMachineDirectory
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlin.collections.set

internal class DefaultRenderingService(
    private val config: AppcuesConfig,
    private val stateMachineDirectory: StateMachineDirectory,
    private val stateMachineFactory: StateMachineFactory,
) : RenderingService {

    private val qualifiedExperiences = hashMapOf<RenderContext, List<Experience>>()
    private val previewExperiences = hashMapOf<RenderContext, Experience>()

    private lateinit var eventTracker: EventTracker

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

        // make a copy (toMap) while we try to show them, so anything else editing the
        // main mapping won't hit a concurrent modification exception
        qualifiedExperiences.toMap().forEach { attemptToShow(it.value) }

        // No caching required for modals since they can't be lazy-loaded.
        qualifiedExperiences.remove(Modal)
    }

    private suspend fun attemptToShow(experiences: List<Experience>) {
        // ensure list is not empty, after that we get the first experience and try to show it.
        // If it does not show we drop that experience and recursively call this again
        if (experiences.isEmpty()) return

        val experience = experiences.first()
        if (show(experience) != ShowExperienceResult.Success) {
            attemptToShow(experiences.drop(1))
        } else {
            eventTracker.trackExperienceRecovery(experience)
        }
    }

    /**
     * returns true/false whether the experience is was started
     */
    override suspend fun show(experience: Experience): ShowExperienceResult = with(experience) {
        var canShow = config.interceptor?.canDisplayExperience(experience.id) ?: true

        // if there is an active experiment, and we should not show this experience (control group), then
        // track the analytics for experiment_entered, but ensure we exit early. This should be checked before
        // we dismiss any current experience below.
        if (experiment?.group == "control") {
            eventTracker.track(experiment)
            canShow = false
        }

        if (!canShow) return ShowExperienceResult.Skip

        val stateMachine = getStateMachine() ?: return ShowExperienceResult.NoRenderContext(experience, renderContext).also {
            eventTracker.trackExperienceError(experience, "no render context $renderContext")
        }

        if (stateMachine.checkPriority(this)) {
            return when (val result = stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false))) {
                is Success -> show(this)
                is Failure -> ShowExperienceResult.RenderError(this, result.reason.message)
            }
        }

        // track an experiment_entered analytic, if exists, since we know it is not in the control group at this point
        experiment?.let { eventTracker.track(it) }

        return when (val result = stateMachine.handleAction(StartExperience(this))) {
            is Success -> ShowExperienceResult.Success.also { previewExperiences.remove(renderContext) }
            is Failure -> ShowExperienceResult.RenderError(this, result.reason.message)
        }
    }

    private fun Experience.getStateMachine(): StateMachine? {
        // try to retrieve state machine
        return stateMachineDirectory.getOwner(renderContext)?.stateMachine ?: run {
            // in case its null we check if the render context is modal
            if (renderContext == Modal) {
                // if it is then we create it (should happen just once)
                val modalOwner = ModalStateMachineOwner(stateMachineFactory.create(eventTracker))
                stateMachineDirectory.setOwner(modalOwner)
                modalOwner.stateMachine
            } else null
        }
    }

    private fun StateMachine.checkPriority(newExperience: Experience): Boolean {
        // "event_trigger" or "forced" experience priority is NORMAL, "screen_view" is low -
        // if an experience is currently showing and the new experience coming in is normal priority
        // then it replaces whatever is currently showing - i.e. an "event_trigger" experience will
        // supersede a "screen_view" triggered experience - per Appcues standard behavior
        return newExperience.priority == NORMAL && state != Idling
    }

    override suspend fun show(renderContext: RenderContext, stepReference: StepReference) {
        stateMachineDirectory.getOwner(renderContext)?.stateMachine?.handleAction(StartStep(stepReference))
    }

    override suspend fun preview(experience: Experience): PreviewExperienceResult {
        previewExperiences[experience.renderContext] = experience

        return when (val result = show(experience)) {
            is ShowExperienceResult.NoRenderContext ->
                PreviewExperienceResult.PreviewDeferred(result.experience, result.renderContext.getFrameId())
            is ShowExperienceResult.RenderError ->
                PreviewExperienceResult.RenderError(experience, result.message)
            is ShowExperienceResult.RequestError ->
                PreviewExperienceResult.RequestError(result.message)
            else -> PreviewExperienceResult.Success
        }
    }

    override suspend fun start(context: RenderContext, frame: AppcuesFrameView) {
        // If there's already a frame for the context, reset it back to its unregistered state.
        stateMachineDirectory.getOwner(context)?.reset()

        // if this frame is already registered for a different context, reset it back - a frame
        // can only be registered to a single RenderContext
        stateMachineDirectory.getOwner(frame)?.let {
            if (it.renderContext != context) {
                it.reset()
            }
        }

        val stateMachine = stateMachineFactory.create(eventTracker) { qualifiedExperiences.remove(context) }

        stateMachineDirectory.setOwner(AppcuesFrameStateMachineOwner(frame, context, stateMachine))

        // shows preview if exist or else show possible experiences from qualification
        previewExperiences[context]?.let { show(it) } ?: qualifiedExperiences[context]?.let { attemptToShow(it) }
    }

    override suspend fun dismiss(renderContext: RenderContext, markComplete: Boolean, destroyed: Boolean) {
        stateMachineDirectory.getOwner(renderContext)?.stateMachine?.handleAction(EndExperience(markComplete, destroyed))
    }

    override fun getExperienceState(renderContext: RenderContext): ExperienceState? {
        val owner = stateMachineDirectory.getOwner(renderContext) ?: return null
        val experience = owner.stateMachine.state.currentExperience
        val stepIndex = owner.stateMachine.state.currentStepIndex

        if (experience == null || stepIndex == null) return null

        return ExperienceState(experience, stepIndex)
    }

    override suspend fun reset() {
        previewExperiences.clear()
        qualifiedExperiences.clear()
        stateMachineDirectory.resetAll()
    }
}
