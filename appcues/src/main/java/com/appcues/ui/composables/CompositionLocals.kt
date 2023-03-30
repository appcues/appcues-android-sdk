package com.appcues.ui.composables

import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.Action
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.logging.Logcues
import com.appcues.trait.AppcuesTraitException
import com.appcues.ui.AppcuesViewModel
import com.appcues.ui.ShakeGestureListener
import com.appcues.ui.composables.StackScope.ColumnStackScope
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
internal val LocalImageLoader = staticCompositionLocalOf<ImageLoader?> { null }

/**
 * LocalAppcuesPagination is used to report back any page change that
 * happened that is coming from outside of our internal SDK logic.
 * Usually used by traits that when dealing with multi-page containers
 */
internal val LocalAppcuesPaginationDelegate = compositionLocalOf { AppcuesPagination {} }

internal data class AppcuesPagination(val onPageChanged: (Int) -> Unit)

internal val LocalViewModel = staticCompositionLocalOf<AppcuesViewModel> { noLocalProvidedFor("AppcuesViewModel") }

internal val LocalShakeGestureListener = staticCompositionLocalOf<ShakeGestureListener> { noLocalProvidedFor("ShakeGestureListener") }

internal val LocalLogcues = staticCompositionLocalOf<Logcues> { noLocalProvidedFor("LocalLogcues") }

internal val LocalExperienceStepFormStateDelegate = compositionLocalOf { ExperienceStepFormState() }

internal val LocalStackScope = compositionLocalOf<StackScope> { ColumnStackScope(null, 0) }

internal val LocalChromeClient = compositionLocalOf { AccompanistWebChromeClient() }

internal val LocalAppcuesTraitExceptionHandler = compositionLocalOf { AppcuesTraitExceptionHandler {} }
internal data class AppcuesTraitExceptionHandler(val onTraitException: (AppcuesTraitException) -> Unit)

internal sealed class StackScope(private val childrenCount: Int) {

    private var childrenSizes = hashMapOf<UUID, Int>()
    private val _greaterSize = mutableStateOf<Float?>(null)
    val greaterSize: State<Float?> = _greaterSize

    fun updateChildSize(id: UUID, size: Int) {
        // if there is only one child we don't collect size
        if (childrenCount <= 1) {
            // negative value means no size will be set
            _greaterSize.value = -1.0f
            return
        }

        // collect size once per each child
        if (!childrenSizes.containsKey(id)) {
            childrenSizes[id] = size
        }

        // when all children have reported its size, update the greaterSize value
        if (childrenSizes.size == childrenCount) {
            _greaterSize.value = childrenSizes.maxOf { it.value.toFloat() }
        }
    }

    class RowStackScope(val height: Double?, childrenCount: Int) : StackScope(childrenCount)
    class ColumnStackScope(val width: Double?, childrenCount: Int) : StackScope(childrenCount)
}

val LocalAppcuesStepMetadata = compositionLocalOf<AppcuesStepMetadata> { noLocalProvidedFor("LocalAppcuesStepMetadata") }

data class AppcuesStepMetadata(
    val previous: Map<String, Any?> = hashMapOf(),
    val actual: Map<String, Any?> = hashMapOf()
)

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
