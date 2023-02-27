package com.appcues.trait.appcues

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSizeIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.TargetRectangleTrait.TargetRectangleInfo
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_END
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_START
import com.appcues.trait.appcues.TooltipPointerPosition.NONE
import com.appcues.trait.appcues.TooltipPointerPosition.TOP
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_END
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_START
import com.appcues.trait.extensions.getContentDistance
import com.appcues.trait.extensions.getRect
import com.appcues.trait.extensions.getTooltipPointerPosition
import com.appcues.trait.extensions.rememberDpStepAnimation
import com.appcues.trait.extensions.rememberFloatStepAnimation
import com.appcues.trait.extensions.rememberTargetRectangleInfo
import com.appcues.ui.AppcuesOverlayViewManager
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.coloredShadowPath
import com.appcues.ui.extensions.getColor
import com.appcues.ui.extensions.getPaddings
import com.appcues.ui.extensions.styleBackground
import com.appcues.ui.modal.dialogEnterTransition
import com.appcues.ui.modal.dialogExitTransition
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.rememberAppcuesWindowInfo
import com.appcues.util.ne
import org.koin.core.scope.Scope

internal class TooltipTrait(
    override val config: AppcuesConfigMap,
    val scope: Scope,
) : PresentingTrait, ContentWrappingTrait {

    companion object {

        const val TYPE = "@appcues/tooltip"

        val SCREEN_HORIZONTAL_PADDING = 12.dp
        val SCREEN_VERTICAL_PADDING = 24.dp
        private val MAX_WIDTH_DP = 350.dp
        private val MAX_HEIGHT_DP = 600.dp
        private const val POINTER_BASE_DEFAULT = 12.0
        private const val POINTER_LENGTH_DEFAULT = 8.0
    }

    private val style = config.getConfigStyle("style")

    // if hidePointer is present, set base and length to 0 Dp
    private val hidePointer = config.getConfigOrDefault("hidePointer", false)
    private val pointerBaseDp = if (hidePointer) 0.dp else config.getConfigOrDefault("pointerBase", POINTER_BASE_DEFAULT).dp
    private val pointerLengthDp = if (hidePointer) 0.dp else config.getConfigOrDefault("pointerLength", POINTER_LENGTH_DEFAULT).dp

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).start()
    }

    @Composable
    override fun WrapContent(content: @Composable (modifier: Modifier, wrapperInsets: PaddingValues) -> Unit) {
        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val windowInfo = rememberAppcuesWindowInfo()
        val targetRectInfo = rememberTargetRectangleInfo(metadata)
        val containerDimens = remember { mutableStateOf<TooltipContainerDimens?>(null) }
        val targetRect = targetRectInfo.getRect(windowInfo)
        val distance = targetRectInfo.getContentDistance()

        val floatAnimation = rememberFloatStepAnimation(metadata)
        val dpAnimation = rememberDpStepAnimation(metadata)

        val tooltipSettings = rememberTooltipSettings(
            windowInfo = windowInfo,
            targetRectangleInfo = targetRectInfo,
            containerDimens = containerDimens.value,
            targetRect = targetRect,
            distance = distance
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SCREEN_HORIZONTAL_PADDING, vertical = SCREEN_VERTICAL_PADDING)
        ) {
            AppcuesTraitAnimatedVisibility(
                visibleState = rememberAppcuesContentVisibility(),
                enter = dialogEnterTransition(),
                exit = dialogExitTransition(),
            ) {
                // positions both the tip and modal of the tooltip on the screen
                Column(
                    modifier = Modifier.positionTooltip(
                        targetRect,
                        containerDimens.value,
                        tooltipSettings,
                        windowInfo,
                        dpAnimation
                    )
                ) {
                    val tooltipPath = tooltipPath(tooltipSettings, containerDimens.value, style, floatAnimation)
                    Box(
                        modifier = Modifier
                            .requiredSizeIn(maxWidth = MAX_WIDTH_DP, maxHeight = MAX_HEIGHT_DP)
                            .tooltipSize(style)
                            .onTooltipSizeChanged(density, containerDimens)
                            .styleShadowPath(style, tooltipPath, isSystemInDarkTheme())
                            .clipToPath(tooltipPath)
                            .styleBackground(style, isSystemInDarkTheme())
                            .styleBorderPath(style, tooltipPath, isSystemInDarkTheme())
                    ) {
                        content(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(style.getPaddings()),
                            wrapperInsets = tooltipSettings.getContentPaddingValues()
                        )
                    }
                }
            }
        }
    }

    private fun Modifier.onTooltipSizeChanged(density: Density, containerDimens: MutableState<TooltipContainerDimens?>) = then(
        Modifier.onSizeChanged {
            with(density) {
                containerDimens.value = TooltipContainerDimens(
                    widthDp = it.width.toDp(),
                    heightDp = it.height.toDp(),
                    widthPx = it.width.toFloat(),
                    heightPx = it.height.toFloat()
                )
            }
        }
    )

    private fun Modifier.clipToPath(path: Path) = then(
        Modifier.clip(GenericShape { _, _ -> addPath(path) })
    )

    private fun Modifier.styleBorderPath(
        style: ComponentStyle?,
        path: Path,
        isDark: Boolean
    ) = this.then(
        if (style?.borderWidth != null && style.borderWidth ne 0.0 && style.borderColor != null) {
            Modifier
                .border(style.borderWidth.dp, style.borderColor.getColor(isDark), GenericShape { _, _ -> addPath(path) })
                .padding(style.borderWidth.dp)
        } else {
            Modifier
        }
    )

    private fun Modifier.positionTooltip(
        targetRect: Rect?,
        containerDimens: TooltipContainerDimens?,
        pointerSettings: TooltipSettings,
        windowInfo: AppcuesWindowInfo,
        animationSpec: FiniteAnimationSpec<Dp>,
    ): Modifier = composed {
        // skip this until we have containerDimens defined
        if (containerDimens == null) return@composed Modifier

        val tooltipPaddingTop = animateDpAsState(
            targetValue = when (pointerSettings.pointerPosition) {
                TOP, TOP_START, TOP_END -> max((targetRect?.bottom?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING + pointerSettings.distance, 0.dp)
                BOTTOM, BOTTOM_START, BOTTOM_END -> max(
                    (targetRect?.top?.dp ?: 0.dp) - containerDimens.heightDp - SCREEN_VERTICAL_PADDING - pointerSettings.distance, 0.dp
                )
                NONE -> windowInfo.heightDp - containerDimens.heightDp - SCREEN_VERTICAL_PADDING
            },
            animationSpec = animationSpec
        )

        val toolTipPaddingStart = animateDpAsState(
            targetValue = if (targetRect != null) {
                // padding start max cant be lower than 0, this causes coerceIn to throw an exception
                val paddingStartMax = max(windowInfo.widthDp - (SCREEN_HORIZONTAL_PADDING * 2) - containerDimens.widthDp, 0.dp)
                val paddingStartOnTarget = targetRect.center.x.dp - SCREEN_HORIZONTAL_PADDING - (containerDimens.widthDp / 2)

                if (paddingStartOnTarget < 0.dp) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget
                } else if (paddingStartOnTarget > paddingStartMax) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget - paddingStartMax
                }

                // target value is between min and max
                paddingStartOnTarget.coerceIn(0.dp, paddingStartMax)
            } else {
                // When no targetRect is found we align the tooltip at bottomCenter of the screen
                val paddingStartCentered = (windowInfo.widthDp - containerDimens.widthDp - (SCREEN_HORIZONTAL_PADDING * 2)) / 2
                // Min value
                paddingStartCentered.coerceAtLeast(0.dp)
            },
            animationSpec = animationSpec
        )

        then(Modifier.padding(start = toolTipPaddingStart.value, top = tooltipPaddingTop.value))
    }

    private fun TooltipSettings.getContentPaddingValues(): PaddingValues {
        return when (pointerPosition) {
            TOP, TOP_START, TOP_END -> PaddingValues(top = pointerLengthDp)
            BOTTOM, BOTTOM_START, BOTTOM_END -> PaddingValues(bottom = pointerLengthDp)
            NONE -> PaddingValues()
        }
    }

    private fun Modifier.styleShadowPath(style: ComponentStyle?, path: Path, isDark: Boolean): Modifier {
        return this.then(
            if (style?.shadow != null)
                Modifier.coloredShadowPath(
                    style.shadow.color.getColor(isDark),
                    path,
                    style.shadow.radius.dp,
                    offsetX = style.shadow.x.dp,
                    offsetY = style.shadow.y.dp
                ) else Modifier
        )
    }

    private fun Modifier.tooltipSize(style: ComponentStyle?): Modifier = then(
        Modifier.size(width = style?.width?.dp ?: Dp.Unspecified, height = style?.height?.dp ?: Dp.Unspecified)
    )

    @Composable
    private fun rememberTooltipSettings(
        windowInfo: AppcuesWindowInfo,
        targetRectangleInfo: TargetRectangleInfo?,
        containerDimens: TooltipContainerDimens?,
        targetRect: Rect?,
        distance: Dp,
    ): TooltipSettings {
        val density = LocalDensity.current
        return remember(targetRectangleInfo, containerDimens) {
            val pointerPosition = targetRectangleInfo.getTooltipPointerPosition(
                windowInfo = windowInfo,
                containerDimens = containerDimens,
                targetRect = targetRect,
                contentDistanceFromTarget = distance
            )

            getTooltipSettings(
                density = density,
                position = pointerPosition,
                distance = distance,
                pointerBaseDp = pointerBaseDp,
                pointerLengthDp = pointerLengthDp
            )
        }
    }
}
