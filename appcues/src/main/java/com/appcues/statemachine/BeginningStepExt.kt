package com.appcues.statemachine

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State.BeginningStep
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.PresentingTrait
import kotlinx.coroutines.delay

internal suspend fun BeginningStep.presentContainer(
    actionProcessor: ActionProcessor,
    actions: List<ExperienceAction>,
): Action {
    // first, process any pre-step actions that need to be handled before the container is presented.
    // for example - navigating to another screen in the app
    actionProcessor.process(actions)

    return try {
        // this could throw an exception on trait failure, handled below
        produceMetadataWithRetry()

        // kick off UI
        presentingTrait()?.run {
            present()
            RenderStep
        } ?: ReportError(toStepError(AppcuesTraitException("Unable to present step $flatStepIndex")), true)
        // the exception above would only happen if the step index was not a valid step in the experience and
        // we could not find the group for that step - should never really happen
    } catch (exception: AppcuesTraitException) {
        ReportError(toStepError(exception), true)
    }
}

internal fun BeginningStep.presentingTrait(): PresentingTrait? {
    return experience.groupLookup[flatStepIndex]?.let { experience.stepContainers[it].presentingTrait }
}

internal fun BeginningStep.toStepError(exception: AppcuesTraitException): StepError {
    return StepError(
        experience = experience,
        stepIndex = flatStepIndex,
        message = exception.message ?: "Unable to render step $flatStepIndex"
    )
}

internal fun BeginningStep.produceMetadata() {
    metadata = hashMapOf<String, Any?>().apply { metadataSettingsTraits().forEach { putAll(it.produceMetadata()) } }
}

private suspend fun BeginningStep.produceMetadataWithRetry() {
    return try {
        metadata = hashMapOf<String, Any?>().apply { metadataSettingsTraits().forEach { putAll(it.produceMetadata()) } }
    } catch (ex: AppcuesTraitException) {
        if (ex.retryMilliseconds != null) {
            delay(ex.retryMilliseconds.toLong())
            produceMetadataWithRetry()
        } else {
            throw ex
        }
    }
}

private fun BeginningStep.metadataSettingsTraits(): List<MetadataSettingTrait> {
    return with(experience) {
        // find the container index
        val containerId = groupLookup[flatStepIndex]
        // find the step index in relation to the container
        val stepIndexInContainer = stepIndexLookup[flatStepIndex]
        // if both are valid ids we return Rendering else null
        if (containerId != null && stepIndexInContainer != null) {
            val container = stepContainers[containerId]
            val step = container.steps[stepIndexInContainer]
            // this will throw if metadata fails
            step.metadataSettingTraits
        } else {
            // should be impossible at this point, but will cover with an exception as well
            throw AppcuesTraitException("Invalid step index $flatStepIndex")
        }
    }
}
