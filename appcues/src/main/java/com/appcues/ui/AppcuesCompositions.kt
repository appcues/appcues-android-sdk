package com.appcues.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Density
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action
import com.appcues.trait.AppcuesPaginationData
import java.util.UUID

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActionDelegate = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: (ExperienceAction) -> Unit)

internal val LocalAppcuesActions = staticCompositionLocalOf<Map<UUID, List<Action>>> { hashMapOf() }

/**
 * LocalAppcuesPagination is used to report back any page change that
 * happened that is coming from outside of our internal SDK logic.
 * Usually used by traits that when dealing with multi-page containers
 */
internal val LocalAppcuesPaginationDelegate = compositionLocalOf { AppcuesPagination {} }

internal data class AppcuesPagination(val onPageChanged: (Int) -> Unit)

internal val appcuesPaginationData = mutableStateOf(AppcuesPaginationData(1, 0, 0.0f))

/**
 * rememberAppcuesPaginationState is used by traits that wants to know updates about pagination data
 * returns a State of AppcuesPaginationData containing pageCount, currentPage index and scrollingOffset
 * that can be used to sync animations
 */
@Composable
internal fun rememberAppcuesPaginationState() = remember<State<AppcuesPaginationData>> { appcuesPaginationData }

internal val isContentVisible = MutableTransitionState(false)

@Composable
internal fun LaunchOnHideAnimationCompleted(block: () -> Unit) {
    with(remember { mutableStateOf(isContentVisible) }.value) {
        // if hide animation is completed
        if (isIdle && currentState.not()) {
            block()
        }
    }
}

// we could make this public to give more flexibility in the future
@Composable
internal fun rememberAppcuesContentVisibility() = remember { isContentVisible }
