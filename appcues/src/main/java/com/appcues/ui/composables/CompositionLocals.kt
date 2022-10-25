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
import com.appcues.ui.AppcuesViewModel
import com.appcues.ui.ShakeGestureListener
import com.appcues.ui.composables.StackScope.ColumnStackScope
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

internal sealed class StackScope(private val childrenCount: Int) {

    private var childrenSizes = hashMapOf<UUID, Int>()
    private val _greaterHeight = mutableStateOf(0)
    val greaterHeight: State<Int> = _greaterHeight

    fun updateChildSize(id: UUID, size: Int) {
        if (!childrenSizes.containsKey(id)) {
            childrenSizes[id] = size
        }

        if (childrenSizes.size == childrenCount) {
            _greaterHeight.value = childrenSizes.maxOf { it.value }
        }
    }

    class RowStackScope(val height: Double?, childrenCount: Int) : StackScope(childrenCount)
    class ColumnStackScope(val width: Double?, childrenCount: Int) : StackScope(childrenCount)
}

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
