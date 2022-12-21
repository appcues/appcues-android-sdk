package com.appcues.trait.appcues

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.ui.AppcuesOverlayViewManager
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.rememberAppcuesContentVisibility
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

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val windowInfo = rememberAppcuesWindowInfo()
        val metadata = LocalAppcuesStepMetadata.current
        val density = LocalDensity.current

        val actualRect = remember(metadata) {
            // current target rect or empty
            (metadata.actual[TargetElementTrait.METADATA_TARGET_RECT] as Rect?)
        }

        val rectTopOfScreen = remember(metadata) {
            // true if rect position is located before center height of the screen
            if (actualRect != null) actualRect.center.y.dp < windowInfo.heightDp / 2 else false
        }

        val tooltipSize = remember { mutableStateOf(IntSize(0, 0)) }

        AppcuesTraitAnimatedVisibility(
            visibleState = rememberAppcuesContentVisibility(),
            enter = dialogEnterTransition(),
            exit = dialogExitTransition(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(SCREEN_PADDING)
                        .positionTooltip(actualRect, tooltipSize.value, rectTopOfScreen, density, windowInfo)
                ) {
                    if (rectTopOfScreen) {
                        Box(
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .size(20.dp)
                                .clip(triangleShape)
                                .background(Color.Green)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .requiredSizeIn(maxWidth = MAX_WIDTH_DP, maxHeight = MAX_HEIGHT_DP)
                            .onSizeChanged { tooltipSize.value = it }
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Red)
                    ) {
                        content(false, PaddingValues(0.dp))
                    }
                    if (!rectTopOfScreen) {
                        Box(
                            modifier = Modifier
                                .padding(start = 32.dp)
                                .size(20.dp)
                                .rotate(degrees = 180f)
                                .clip(triangleShape)
                                .background(Color.Green)
                        )
                    }
                }
            }
        }
    }

    private val triangleShape = GenericShape { size, _ ->
        // 1)
        moveTo(size.width / 2f, 0f)

        // 2)
        lineTo(size.width, size.height)

        // 3)
        lineTo(0f, size.height)
    }

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).addView()
    }

    @Composable
    private fun Modifier.positionTooltip(
        actualRect: Rect?,
        tooltipSize: IntSize,
        rectTopOfScreen: Boolean,
        density: Density,
        windowInfo: AppcuesWindowInfo
    ): Modifier {
        val tooltipPaddingTop = animateDpAsState(
            targetValue = with(density) {
                if (actualRect != null) {
                    if (rectTopOfScreen) {
                        max((actualRect.bottom.dp), 0.dp)
                    } else {
                        max((actualRect.top.dp - tooltipSize.height.dp - SCREEN_PADDING - 20.dp), 0.dp)
                    }
                } else {
                    windowInfo.heightDp - tooltipSize.height.dp - SCREEN_PADDING
                }
            }
        )

        val toolTipPaddingStart = animateDpAsState(
            targetValue = with(density) {
                if (actualRect != null) {
                    max(0.dp, 0.dp)
                } else {
                    max((windowInfo.widthDp - (SCREEN_PADDING * 2) - tooltipSize.width.dp) / 2, 0.dp)
                }
            }
        )

        return then(Modifier.padding(start = toolTipPaddingStart.value, top = tooltipPaddingTop.value))
    }
}
