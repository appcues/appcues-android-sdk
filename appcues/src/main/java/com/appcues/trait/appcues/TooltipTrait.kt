package com.appcues.trait.appcues

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.BOTTOM
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.NONE
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.TOP
import com.appcues.ui.AppcuesOverlayViewManager
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.styleBackground
import com.appcues.ui.modal.dialogEnterTransition
import com.appcues.ui.modal.dialogExitTransition
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import org.koin.core.scope.Scope

internal class TooltipTrait(
    override val config: AppcuesConfigMap,
    val scope: Scope,
) : PresentingTrait, ContentWrappingTrait {

    companion object {

        const val TYPE = "@appcues/tooltip"

        private val SCREEN_PADDING = 16.dp
        private val MAX_WIDTH_DP = 350.dp
        private val MAX_HEIGHT_DP = 200.dp
    }

    private enum class PointerPosition {
        TOP, BOTTOM, NONE
    }

    private val style = config.getConfigStyle("style")

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).addView()
    }

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val windowInfo = rememberAppcuesWindowInfo()
        val metadata = LocalAppcuesStepMetadata.current
        val density = LocalDensity.current

        val actualRect = remember(metadata) {
            // current target rect or empty
            (metadata.actual[TargetElementTrait.METADATA_TARGET_RECT] as Rect?)
        }

        val pointerPosition = remember(metadata) {
            when {
                actualRect == null -> PointerPosition.NONE
                actualRect.center.y.dp < windowInfo.heightDp / 2 -> PointerPosition.TOP
                else -> PointerPosition.BOTTOM
            }
        }

        val tooltipSize = remember { mutableStateOf(DpSize(0.dp, 0.dp)) }

        AppcuesTraitAnimatedVisibility(
            visibleState = rememberAppcuesContentVisibility(),
            enter = dialogEnterTransition(),
            exit = dialogExitTransition(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(SCREEN_PADDING)
                        .positionTooltip(actualRect, tooltipSize.value, pointerPosition, windowInfo)
                ) {
                    if (pointerPosition == TOP) {
                        Box(
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .size(20.dp)
                                .clip(triangleShape)
                                .styleBackground(style, isSystemInDarkTheme())
                        )
                    }
                    Box(
                        modifier = Modifier
                            .requiredSizeIn(maxWidth = MAX_WIDTH_DP, maxHeight = MAX_HEIGHT_DP)
                            .onSizeChanged { with(density) { tooltipSize.value = DpSize(it.width.toDp(), it.height.toDp()) } }
                            .clip(RoundedCornerShape(8.dp))
                            .styleBackground(style, isSystemInDarkTheme())
                    ) {
                        content(false, PaddingValues(0.dp))
                    }
                    if (pointerPosition == BOTTOM) {
                        Box(
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .size(20.dp)
                                .rotate(degrees = 180f)
                                .clip(triangleShape)
                                .styleBackground(style, isSystemInDarkTheme())
                        )
                    }
                }
            }
        }
    }

    private val triangleShape = GenericShape { size, _ ->
        moveTo(size.width / 2f, 0f)
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
    }

    private fun Modifier.positionTooltip(
        actualRect: Rect?,
        tooltipSize: DpSize,
        pointerPosition: PointerPosition,
        windowInfo: AppcuesWindowInfo
    ): Modifier = composed {
        val tooltipPaddingTop = animateDpAsState(
            targetValue = when (pointerPosition) {
                TOP -> max((actualRect?.let { it.bottom.dp } ?: 0.dp), 0.dp)
                BOTTOM -> max((actualRect?.let { it.top.dp - tooltipSize.height - SCREEN_PADDING - 20.dp } ?: 0.dp), 0.dp)
                NONE -> windowInfo.heightDp - tooltipSize.height - SCREEN_PADDING
            }
        )

        val toolTipPaddingStart = animateDpAsState(
            targetValue = if (actualRect != null) {
                max(0.dp, 0.dp)
            } else {
                max((windowInfo.widthDp - (SCREEN_PADDING * 2) - tooltipSize.width) / 2, 0.dp)
            }
        )

        then(Modifier.padding(start = toolTipPaddingStart.value, top = tooltipPaddingTop.value))
    }
}
