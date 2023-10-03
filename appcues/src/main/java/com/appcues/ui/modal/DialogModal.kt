package com.appcues.ui.modal

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.data.model.styling.ComponentStyle.ComponentHorizontalAlignment
import com.appcues.data.model.styling.ComponentStyle.ComponentVerticalAlignment
import com.appcues.trait.AppcuesContentAnimatedVisibility
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

private val MAX_WIDTH_COMPACT_DP = 400.dp
private val MAX_WIDTH_MEDIUM_DP = 480.dp
private val MAX_WIDTH_EXPANDED_DP = 560.dp
private val MAX_HEIGHT_COMPACT_DP = Dp.Unspecified
private val MAX_HEIGHT_MEDIUM_DP = 800.dp
private val MAX_HEIGHT_EXPANDED_DP = 900.dp
private const val SCREEN_PADDING = 0.05

internal enum class DialogTransition {
    FADE, SLIDE
}

internal enum class SlideTransitionEdge {
    LEADING, TRAILING, TOP, BOTTOM, CENTER
}

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
        AppcuesContentAnimatedVisibility(
            modifier = Modifier.align(style.getBoxAlignment()),
            enter = transition.toEnterTransition(style, horizontalPaddingPx, verticalPaddingPx),
            exit = transition.toExitTransition(style, horizontalPaddingPx, verticalPaddingPx),
        ) {
            Surface(
                modifier = Modifier
                    .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                    // default modal style modifiers
                    .modalStyle(style, isDark) { Modifier.dialogModifier(it, isDark) }
                    // apply width AFTER any margin in modalStyle in line above
                    .styleWidth(style)
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

private fun DialogTransition.toEnterTransition(style: ComponentStyle?, horizontalPadding: Int, verticalPadding: Int): EnterTransition {
    return when (this) {
        SLIDE -> slideOutEnterTransition(style.getSlideEdge(), horizontalPadding, verticalPadding)
        FADE -> dialogEnterTransition()
    }
}

private fun DialogTransition.toExitTransition(style: ComponentStyle?, horizontalPadding: Int, verticalPadding: Int): ExitTransition {
    return when (this) {
        // for the exit edge, if no fixed exit given but a fixed enter edge was used, fall back to that
        SLIDE ->
            slideOutExitTransition(style.getSlideEdge(), horizontalPadding, verticalPadding)
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
