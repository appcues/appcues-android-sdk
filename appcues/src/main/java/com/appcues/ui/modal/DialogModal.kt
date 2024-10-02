package com.appcues.ui.modal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SplineBasedFloatDecayAnimationSpec
import androidx.compose.animation.core.generateDecayAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.trait.AppcuesContentAnimatedVisibility
import com.appcues.ui.composables.AppcuesDismissalDelegate
import com.appcues.ui.composables.LocalAppcuesDismissalDelegate
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.modalStyle
import com.appcues.ui.modal.DialogTransition.FADE
import com.appcues.ui.modal.DialogTransition.SLIDE
import com.appcues.ui.modal.SlideTransitionEdge.BOTTOM
import com.appcues.ui.modal.SlideTransitionEdge.CENTER
import com.appcues.ui.modal.SlideTransitionEdge.LEADING
import com.appcues.ui.modal.SlideTransitionEdge.TOP
import com.appcues.ui.modal.SlideTransitionEdge.TRAILING
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM
import kotlin.math.roundToInt

private val MAX_WIDTH_COMPACT_DP = 400.dp
private val MAX_WIDTH_MEDIUM_DP = 480.dp
private val MAX_WIDTH_EXPANDED_DP = 560.dp
private val MAX_HEIGHT_COMPACT_DP = Dp.Unspecified
private val MAX_HEIGHT_MEDIUM_DP = 800.dp
private val MAX_HEIGHT_EXPANDED_DP = 900.dp
private const val SCREEN_PADDING = 0.05
private const val NON_DISMISSIBLE_ANCHOR_RATIO = 0.05f
private const val ANCHOR_POSITIONAL_THRESHOLD_RATIO = 0.5f
private const val ANCHOR_VELOCITY_THRESHOLD = 100

internal enum class DialogTransition {
    FADE, SLIDE
}

internal enum class SlideTransitionEdge {
    LEADING, TRAILING, TOP, BOTTOM, CENTER
}

internal enum class DragAnchors {
    Dismissed,
    Presenting,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DialogModal(
    style: ComponentStyle?,
    content: @Composable (
        modifier: Modifier,
        containerPadding: PaddingValues,
        safeAreaInsets: PaddingValues,
        hasVerticalScroll: Boolean,
    ) -> Unit,
    windowInfo: AppcuesWindowInfo,
    transition: DialogTransition,
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    val dismissalDelegate = LocalAppcuesDismissalDelegate.current
    val contentCanDismiss = dismissalDelegate.canDismiss

    val maxWidth = maxWidthDerivedOf(windowInfo)
    val maxHeight = maxHeightDerivedOf(windowInfo)

    // fixed with is used for slideout style dialogs
    val isFixedWidth = (style?.width ?: -1.0) > 0.0

    val dialogHorizontalPadding = if (isFixedWidth) {
        // fixed value for fixed width slideouts
        12.dp
    } else {
        // normal dialog modals use horizontal padding wih a scale factor based on device width
        val configuration = LocalConfiguration.current
        (configuration.screenWidthDp * SCREEN_PADDING).dp
    }

    val dialogVerticalPadding = if (isFixedWidth) {
        // fixed value for fixed width slideouts
        12.dp
    } else {
        // normal dialog modals use standard 24.dp vertical padding
        24.dp
    }

    val horizontalPaddingPx = with(density) { dialogHorizontalPadding.roundToPx() }
    val verticalPaddingPx = with(density) { dialogVerticalPadding.roundToPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = dialogHorizontalPadding, vertical = dialogVerticalPadding)
    ) {
        val slideTransitionEdge = style.getSlideEdge()
        val draggableState = remember { transition.anchoredDraggableState(dismissalDelegate, density, slideTransitionEdge) }

        AppcuesContentAnimatedVisibility(
            modifier = Modifier
                .align(style.getBoxAlignment())
                .onSizeChanged { layoutSize ->
                    draggableState?.onSizeChanged(contentCanDismiss, layoutSize, slideTransitionEdge)
                }
                .handleSwipeToDismiss(slideTransitionEdge, draggableState),
            enter = transition.toEnterTransition(slideTransitionEdge, horizontalPaddingPx, verticalPaddingPx),
            exit = transition.toExitTransition(slideTransitionEdge, horizontalPaddingPx, verticalPaddingPx),
        ) {
            Surface(
                modifier = Modifier
                    .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                    // default modal style modifiers
                    .modalStyle(style, isDark) { Modifier.dialogModifier(it, isDark) }
                    // apply width AFTER any margin in modalStyle in line above
                    .styleWidth(style),
                color = Color.Transparent,
            ) {
                content(
                    Modifier,
                    style.getPaddings(),
                    PaddingValues(),
                    true, // support vertical scroll
                )
            }
        }
    }
}

private fun DialogTransition.toEnterTransition(
    slideTransitionEdge: SlideTransitionEdge,
    horizontalPadding: Int,
    verticalPadding: Int
): EnterTransition {
    return when (this) {
        SLIDE -> slideOutEnterTransition(slideTransitionEdge, horizontalPadding, verticalPadding)
        FADE -> dialogEnterTransition()
    }
}

private fun DialogTransition.toExitTransition(
    slideTransitionEdge: SlideTransitionEdge,
    horizontalPadding: Int,
    verticalPadding: Int
): ExitTransition {
    return when (this) {
        // for the exit edge, if no fixed exit given but a fixed enter edge was used, fall back to that
        SLIDE ->
            slideOutExitTransition(slideTransitionEdge, horizontalPadding, verticalPadding)
        FADE -> dialogExitTransition()
    }
}

// determine slide edge based on content alignment
private fun ComponentStyle?.getSlideEdge(): SlideTransitionEdge {
    return when (this?.horizontalAlignment ?: ComponentHorizontalAlignment.CENTER) {
        ComponentHorizontalAlignment.LEADING -> LEADING
        ComponentHorizontalAlignment.TRAILING -> TRAILING
        ComponentHorizontalAlignment.CENTER -> {
            when (this?.verticalAlignment ?: ComponentVerticalAlignment.CENTER) {
                ComponentVerticalAlignment.TOP -> TOP
                ComponentVerticalAlignment.BOTTOM -> BOTTOM
                ComponentVerticalAlignment.CENTER -> CENTER
            }
        }
    }
}

private fun Modifier.styleWidth(
    style: ComponentStyle?,
) = this.then(
    when {
        // if a positive width value is specified, use it, otherwise fill width
        style?.width != null -> if (style.width < 0.0) Modifier.fillMaxWidth() else Modifier.width(style.width.dp)
        else -> Modifier
    }
)

private fun maxWidthDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenWidthType) {
            COMPACT -> MAX_WIDTH_COMPACT_DP
            MEDIUM -> MAX_WIDTH_MEDIUM_DP
            EXPANDED -> MAX_WIDTH_EXPANDED_DP
        }
    }
}

private fun maxHeightDerivedOf(windowInfo: AppcuesWindowInfo): State<Dp> {
    return derivedStateOf {
        when (windowInfo.screenHeightType) {
            COMPACT -> MAX_HEIGHT_COMPACT_DP
            MEDIUM -> MAX_HEIGHT_MEDIUM_DP
            EXPANDED -> MAX_HEIGHT_EXPANDED_DP
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.handleSwipeToDismiss(
    edge: SlideTransitionEdge,
    state: AnchoredDraggableState<DragAnchors>?,
) = this.then(
    // state will only be non-null in the cases where swipe to dismiss was previously
    // determined to be enabled (i.e. slideouts from edge). Thus, the base case here
    // in the else clause is to just do nothing, typically.

    when {
        state != null && (edge == LEADING || edge == TRAILING) -> Modifier
            .offset {
                IntOffset(
                    x = state
                        .requireOffset()
                        .roundToInt(), y = 0
                )
            }
            .anchoredDraggable(state, Orientation.Horizontal)
        state != null && (edge == TOP || edge == BOTTOM) -> Modifier
            .offset {
                IntOffset(
                    x = 0, y = state
                        .requireOffset()
                        .roundToInt()
                )
            }
            .anchoredDraggable(state, Orientation.Vertical)
        else -> Modifier
    }
)

@OptIn(ExperimentalFoundationApi::class)
private fun AnchoredDraggableState<DragAnchors>.onSizeChanged(
    canDismiss: Boolean,
    size: IntSize,
    slideTransitionEdge: SlideTransitionEdge
) {
    // This function updates the ending point for the drag anchor (dismissal) based on the size of the content
    // and which edge it is allowed to swipe away to. It would only be called on an AnchoredDraggableState that
    // was non-null, created previously when it was determined this content may support swipe to dismiss,
    // i.e. slideouts from edge. The swipe may still be rejected with a little bounce feedback, if the step
    // is not dismissible.

    val width = size.width
    val height = size.height

    // if the content can be dismissed, the dismiss anchor is 2x the height/width of content (depending on directly)
    // this is so that the content must be fully moved off screen to transition to the dismiss anchor
    // (velocity will also impact this and allow expected fling behavior)
    //
    // if the content cannot be dismissed, a tiny scale factor is used to just provide bounce feedback that it
    // cannot be swiped away
    val scaleFactor = if (canDismiss) 2.0f else NON_DISMISSIBLE_ANCHOR_RATIO

    val dismissed = when (slideTransitionEdge) {
        LEADING -> -1 * width * scaleFactor
        TRAILING -> width * scaleFactor
        TOP -> -1 * height * scaleFactor
        BOTTOM -> height * scaleFactor
        CENTER -> null
    }

    if (dismissed != null) {
        updateAnchors(
            DraggableAnchors {
                DragAnchors.Presenting at 0f
                DragAnchors.Dismissed at dismissed.toFloat()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun DialogTransition.anchoredDraggableState(
    dismissalDelegate: AppcuesDismissalDelegate,
    density: Density,
    slideTransitionEdge: SlideTransitionEdge
): AnchoredDraggableState<DragAnchors>? {
    // Only in the case of slideouts from the edge, we'll set up a draggable state
    // here to potentially allow swipe to dismiss. In the other base case, null is returned - no drag.

    return when {
        this == SLIDE && slideTransitionEdge != CENTER -> {
            AnchoredDraggableState(
                initialValue = DragAnchors.Presenting,
                positionalThreshold = { distance: Float -> distance * ANCHOR_POSITIONAL_THRESHOLD_RATIO },
                velocityThreshold = { with(density) { ANCHOR_VELOCITY_THRESHOLD.dp.toPx() } },
                snapAnimationSpec = tween(),
                decayAnimationSpec = SplineBasedFloatDecayAnimationSpec(density).generateDecayAnimationSpec(),
                confirmValueChange = { anchor: DragAnchors ->
                    when {
                        // only allow change to dismissed anchor if the view model
                        // allows this step content to be dismissed
                        anchor == DragAnchors.Dismissed && dismissalDelegate.canDismiss -> {
                            // it is allowed, request the content to be dismissed
                            dismissalDelegate.requestDismissal()
                            true
                        }
                        anchor == DragAnchors.Dismissed -> false
                        else -> true
                    }
                }
            )
        }
        else -> null
    }
}
