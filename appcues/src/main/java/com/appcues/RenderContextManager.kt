package com.appcues

import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import com.appcues.statemachine.StateMachine
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import java.lang.ref.WeakReference

internal class RenderContextManager(override val scope: Scope) : KoinScopeComponent {

    private val lifecycleTracker by inject<ExperienceLifecycleTracker>()

    private val frameSlots: HashMap<RenderContext, WeakReference<AppcuesFrameView>> = hashMapOf()

    private val stateMachineSlots: HashMap<RenderContext, StateMachine> = hashMapOf()

    private val potentiallyRenderableExperiences = hashMapOf<RenderContext, List<Experience>>()

    fun registerEmbedFrame(frameId: String, frame: AppcuesFrameView) {
        val renderContext = Embed(frameId)

        // silently removing the existing state machine to ensure we get a new one when trying
        // to start an experience for this render context
        stateMachineSlots.remove(renderContext)
        frameSlots[Embed(frameId)] = WeakReference(frame)
    }

    fun getEmbedFrame(renderContext: RenderContext): AppcuesFrameView? {
        return when (renderContext) {
            is Embed -> frameSlots[renderContext]?.get()
            else -> null
        }
    }

    fun getOrCreateStateMachines(renderContext: RenderContext): StateMachine {
        return stateMachineSlots[renderContext] ?: run {
            get<StateMachine>()
                .also { stateMachineSlots[renderContext] = it }
                .also { lifecycleTracker.start(it, { potentiallyRenderableExperiences.remove(renderContext) }) }
        }
    }

    fun getStateMachine(renderContext: RenderContext): StateMachine? {
        return stateMachineSlots[renderContext]
    }

    fun putExperiences(experiences: List<Experience>, trigger: ExperienceTrigger) {
        if (trigger is Qualification && trigger.reason == "screen_view") {
            // clear list in case this was a screen_view qualification
            potentiallyRenderableExperiences.clear()

            stateMachineSlots.forEach { map ->
                val renderContext = map.key
                if (renderContext is Embed && getEmbedFrame(map.key) == null) {
                    stateMachineSlots.remove(map.key)
                }
            }
        }

        // populate new experiences into the dictionary
        experiences.filter { it.renderContext !is RenderContext.Modal }.groupBy { it.renderContext }.forEach {
            potentiallyRenderableExperiences[it.key] = it.value
        }
    }

    fun getPotentialExperiences(renderContext: RenderContext): List<Experience> {
        return potentiallyRenderableExperiences[renderContext] ?: arrayListOf()
    }

    fun stop() {
        stateMachineSlots.values.forEach { it.stop() }
    }
}
