package com.appcues.ui.composables

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.Action
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.logging.Logcues
import com.appcues.ui.presentation.AppcuesViewModel
import com.google.accompanist.web.AccompanistWebChromeClient
import java.util.UUID

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActionDelegate = staticCompositionLocalOf<AppcuesActionsDelegate> {
    noLocalProvidedFor("LocalAppcuesActionDelegate")
}

// interface to facilitate understanding the bridge
// between LocalAppcuesActionDelegate and the AppcuesViewModel
internal interface AppcuesActionsDelegate {

    fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?)
}

internal class DefaultAppcuesActionsDelegate(private val viewModel: AppcuesViewModel) : AppcuesActionsDelegate {

    override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        viewModel.onActions(actions, interactionType, viewDescription)
    }
}

internal val LocalAppcuesActions = staticCompositionLocalOf<Map<UUID, List<Action>>> { hashMapOf() }

// used to support UI testing and mocking of image loading
internal val LocalImageLoader = staticCompositionLocalOf<ImageLoader> { noLocalProvidedFor("ImageLoader") }

/**
 * LocalAppcuesPagination is used to report back any page change that
 * happened that is coming from outside of our internal SDK logic.
 * Usually used by traits that when dealing with multi-page containers
 */
internal val LocalAppcuesPaginationDelegate = compositionLocalOf { AppcuesPagination {} }

internal data class AppcuesPagination(val onPageChanged: (Int) -> Unit)

internal val LocalViewModel = staticCompositionLocalOf<AppcuesViewModel> { noLocalProvidedFor("AppcuesViewModel") }

internal val LocalLogcues = staticCompositionLocalOf<Logcues> { noLocalProvidedFor("LocalLogcues") }

internal val LocalExperienceCompositionState = staticCompositionLocalOf<ExperienceCompositionState> {
    noLocalProvidedFor("LocalExperienceCompositionState")
}

internal val LocalExperienceStepFormStateDelegate = compositionLocalOf { ExperienceStepFormState() }

internal val LocalStackScope = compositionLocalOf { StackScope.COLUMN }

internal val LocalChromeClient = compositionLocalOf { AccompanistWebChromeClient() }

internal enum class StackScope {
    ROW, COLUMN
}

/**
 * Use LocalAppcuesStepMetadata to access the shared information between steps
 *
 * Example usage:
 * @sample com.appcues.trait.appcues.BackdropKeyholeTrait
 */
internal val LocalAppcuesStepMetadata: ProvidableCompositionLocal<AppcuesStepMetadata> =
    compositionLocalOf { noLocalProvidedFor("LocalAppcuesStepMetadata") }

/**
 * AppcuesStepMetadata used to share trait information between traits, generated from produceMetadata.
 *
 * Previous and current refer to the previously generated metadata values and the current values.
 * It may be desired to reference both values to support transitions between them.
 */
internal data class AppcuesStepMetadata(
    /**
     * Previously generated metadata values
     */
    val previous: Map<String, Any?> = hashMapOf(),

    /**
     * Current metadata values
     */
    val current: Map<String, Any?> = hashMapOf()
)

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
