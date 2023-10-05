package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.statemachine.Action
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.SideEffect
import com.appcues.trait.AppcuesTraitException
import kotlinx.coroutines.delay

internal data class PresentationEffect(
    private val experience: Experience,
    private val flatStepIndex: Int,
    private val stepContainerIndex: Int,
    private val isDifferentContainer: Boolean,
) : SideEffect {

    override suspend fun launch(processor: ActionProcessor): Action {
        if (stepContainerIndex != 0 || experience.trigger !is Qualification) {
            // for pre-step navigation actions - only allow these to execute if this experience is being launched for some
            // other reason than qualification (i.e. deep links, preview, manual show). For any qualified experience, the initial
            // starting state of the experience is determined solely by flow settings determining the trigger
            // (i.e. trigger on certain screen).
            processor.process(experience.getNavigationActions(stepContainerIndex))
        }

        return tryToRenderStep()
    }

    private suspend fun tryToRenderStep(): Action {
        val presentingTrait = experience.getPresentingTrait(flatStepIndex)

        try {
            val metadata: Map<String, Any?>

            // if we are presenting, we try to produce metadata for some time
            // before failing, or else we just try to produce it with no retry
            if (isDifferentContainer) {
                metadata = produceMetadataWithRetry()

                presentingTrait.present()
            } else {
                metadata = produceMetadata()
            }

            return RenderStep(metadata)
        } catch (exception: AppcuesTraitException) {
            presentingTrait.remove()

            return ReportError(ExperienceError(experience, exception.message), true)
        }
    }

    /**
     * produces metadata but it can retry when catching AppcuesTraitException
     * with retryMilliseconds != null
     *
     * its up to the one responsible for throwing the exception to stop sending
     * a non-null value for retryMilliseconds at some point to avoid infinite loop
     */
    private suspend fun produceMetadataWithRetry(): Map<String, Any?> {
        return try {
            produceMetadata()
        } catch (ex: AppcuesTraitException) {
            if (ex.retryMilliseconds != null) {
                delay(ex.retryMilliseconds.toLong())
                produceMetadataWithRetry()
            } else {
                throw ex
            }
        }
    }

    /**
     * creates a map that contains a combined result of all
     * metadata setting traits for this step
     */
    private fun produceMetadata(): Map<String, Any?> {
        return hashMapOf<String, Any?>().apply {
            experience.getMetadataSettingTraits(flatStepIndex).forEach {
                putAll(it.produceMetadata())
            }
        }
    }
}
