package com.appcues.ui.composables

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.logging.Logcues
import com.appcues.ui.AppcuesViewModel
import com.appcues.ui.ShakeGestureListener
import java.util.UUID

// used to register callback for all Actions triggered from primitives
internal val LocalAppcuesActionDelegate = staticCompositionLocalOf { AppcuesActions {} }

internal data class AppcuesActions(val onAction: (ExperienceAction) -> Unit)

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

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
