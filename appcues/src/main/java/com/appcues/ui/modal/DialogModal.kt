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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
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
import com.appcues.ui.modal.TransitionEdge.BOTTOM
import com.appcues.ui.modal.TransitionEdge.CENTER
import com.appcues.ui.modal.TransitionEdge.LEADING
import com.appcues.ui.modal.TransitionEdge.TOP
import com.appcues.ui.modal.TransitionEdge.TRAILING
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

private val SCREEN_HORIZONTAL_PADDING = 12.dp
private val SCREEN_VERTICAL_PADDING = 24.dp

internal data class DialogTransition(
    val type: String,
    val slideInEdge: String?,
    val slideOutEdge: String?,
)

internal enum class TransitionEdge {
    LEADING, TRAILING, TOP, BOTTOM, CENTER
}

@Composable
internal fun DialogModal(
    style: ComponentStyle?,
    content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit,
    windowInfo: AppcuesWindowInfo,
    transition: DialogTransition,
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current

    val maxWidth = maxWidthDerivedOf(windowInfo)
    val maxHeight = maxHeightDerivedOf(windowInfo)

    val horizontalPaddingPx = with(density) { SCREEN_HORIZONTAL_PADDING.roundToPx() }
    val verticalPaddingPx = with(density) { SCREEN_VERTICAL_PADDING.roundToPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = SCREEN_VERTICAL_PADDING)
    ) {
        AppcuesContentAnimatedVisibility(
            modifier = Modifier.align(style.getBoxAlignment()),
            enter = transition.toEnterTransition(style, horizontalPaddingPx, verticalPaddingPx),
            exit = transition.toExitTransition(style, horizontalPaddingPx, verticalPaddingPx),
        ) {
            Surface(
                modifier = Modifier
                    .sizeIn(maxWidth = maxWidth.value, maxHeight = maxHeight.value)
                    .styleWidth(style)
                    // default modal style modifiers
                    .modalStyle(style, isDark) { Modifier.dialogModifier(it, isDark) }
            ) {
                content(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    containerPadding = style.getPaddings(),
                    safeAreaInsets = PaddingValues()
                )
            }
        }
    }
}

private fun DialogTransition.toEnterTransition(style: ComponentStyle?, horizontalPadding: Int, verticalPadding: Int): EnterTransition {
    return when (this.type) {
        "slide" ->
            slideOutEnterTransition(style.getSlideEdge(this.slideInEdge), horizontalPadding, verticalPadding)
        else -> dialogEnterTransition()
    }
}

private fun DialogTransition.toExitTransition(style: ComponentStyle?, horizontalPadding: Int, verticalPadding: Int): ExitTransition {
    return when (this.type) {
        // for the exit edge, if no fixed exit given but a fixed enter edge was used, fall back to that
        "slide" ->
            slideOutExitTransition(style.getSlideEdge(this.slideOutEdge ?: this.slideInEdge), horizontalPadding, verticalPadding)
        else -> dialogExitTransition()
    }
}

// determine slide edge based on config, or default based on alignment
private fun ComponentStyle?.getSlideEdge(fixed: String?): TransitionEdge {
    val fixedEdge = fixed?.let {
        when (it) {
            "top" -> TOP
            "bottom" -> BOTTOM
            "leading" -> LEADING
            "trailing" -> TRAILING
            "center" -> CENTER
            else -> null
        }
    }

    if (fixedEdge != null) return fixedEdge

    // if no fixed edge provided, find the best default based on the content alignment
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
