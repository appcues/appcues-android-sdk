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
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigOrDefault
import com.appcues.data.model.getConfigStyle
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.trait.AppcuesTraitAnimatedVisibility
import com.appcues.trait.ContentWrappingTrait
import com.appcues.trait.PresentingTrait
import com.appcues.trait.appcues.TooltipPointerPosition.Bottom
import com.appcues.trait.appcues.TooltipPointerPosition.Left
import com.appcues.trait.appcues.TooltipPointerPosition.None
import com.appcues.trait.appcues.TooltipPointerPosition.Right
import com.appcues.trait.appcues.TooltipPointerPosition.Top
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

        internal val SCREEN_HORIZONTAL_PADDING = 12.dp
        internal val SCREEN_VERTICAL_PADDING = 24.dp

        private const val POINTER_BASE_DEFAULT = 16.0
        private const val POINTER_LENGTH_DEFAULT = 8.0

        private val MAX_TOOLTIP_WIDTH = 400.0.dp
    }

    private val style = config.getConfigStyle("style")

    // if hidePointer is present, set base and length to 0 Dp
    private val hidePointer = config.getConfigOrDefault("hidePointer", false)
    private val pointerBaseDp = if (hidePointer) 0.dp else config.getConfigOrDefault("pointerBase", POINTER_BASE_DEFAULT).dp
    private val pointerLengthDp = if (hidePointer) 0.dp else config.getConfigOrDefault("pointerLength", POINTER_LENGTH_DEFAULT).dp
    private val pointerCornerRadius = if (hidePointer) 0.dp else
        config.getConfigOrDefault("pointerCornerRadius", 0.0).dp.coerceInMaxRadius(pointerBaseDp, pointerLengthDp)

    // figures out the max corner radius for this shape and returns the lesser value between
    // the pointerCornerRadius and the calculated max.
    private fun Dp.coerceInMaxRadius(pointerBaseDp: Dp, pointerLengthDp: Dp): Dp {
        return min(this, calculateTooltipMaxRadius(pointerBaseDp.value, pointerLengthDp.value).dp)
    }

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).start()
    }

    @Composable
    override fun WrapContent(
        content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit
    ) {
        val metadata = LocalAppcuesStepMetadata.current
        val targetRectInfo = rememberTargetRectangleInfo(metadata)
        val windowInfo = rememberAppcuesWindowInfo()
        val targetRect = targetRectInfo.getRect(windowInfo)
        val distance = targetRectInfo.getContentDistance()

        // This is the size of the full container, including the pointer length on whatever edge it is applied
        val containerDimens = remember { mutableStateOf<TooltipContainerDimens?>(null) }

        // This is like containerDimens, but removing the size of the pointer from the width or height,
        // depending on the pointer position - used in layout calculation of desired pointer position.
        //
        // Would have liked this to be a derivedStateOf using containerDimens, but there is a chicken/egg
        // problem, as contentDimens needs the tooltipPointerPosition to calculate, but to get that pointer
        // position in the first place, it needs contentDimens. So, it is set in onTooltipSizeChanged like
        // containerDimens
        val contentDimens = remember { mutableStateOf<TooltipContentDimens?>(null) }

        // This computed boolean indicates that content has been sized once, meaning the tooltip pointer
        // position can be calculated. It is then not recalculated again until the step changes. Once a pointer
        // position is decided for the given step rendering, it remains in that position to avoid extra
        // layout passes flipping the position.
        val contentSized = remember(targetRectInfo) { mutableStateOf(false) }

        val floatAnimation = rememberFloatStepAnimation(metadata)
        val dpAnimation = rememberDpStepAnimation(metadata)

        val tooltipPointerPosition = rememberTooltipPointerPosition(
            windowInfo = windowInfo,
            targetRectangleInfo = targetRectInfo,
            contentDimens = contentDimens.value,
            targetRect = targetRect,
            distance = distance,
            pointerLength = pointerLengthDp,
            contentSized = contentSized.value,
        )

        val tooltipSettings = rememberTooltipSettings(
            targetRectangleInfo = targetRectInfo,
            containerDimens = containerDimens.value,
            distance = distance,
            position = tooltipPointerPosition,
        )

        val tooltipPath = drawTooltipPointerPath(tooltipSettings, containerDimens.value, floatAnimation, dpAnimation)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(SCREEN_HORIZONTAL_PADDING, SCREEN_VERTICAL_PADDING)
        ) {
            AppcuesTraitAnimatedVisibility(
                visibleState = rememberAppcuesContentVisibility(),
                enter = dialogEnterTransition(),
                exit = dialogExitTransition(),
            ) {
                // positions both the tip and modal of the tooltip on the screen
                Column(
                    modifier = Modifier.positionTooltip(targetRect, contentDimens.value, tooltipSettings, windowInfo, dpAnimation)
                ) {
                    Box(
                        modifier = Modifier
                            .tooltipSize(style, windowInfo, tooltipSettings, targetRect == null)
                            .onTooltipSizeChanged(style, containerDimens, contentDimens, tooltipPointerPosition, tooltipSettings) {
                                // when tooltip is sized, set contentSized to true so the rest of the
                                // composition can use sized information to place the tooltip.
                                contentSized.value = true
                            }
                            .styleShadowPath(style, tooltipPath, isSystemInDarkTheme())
                            .clipToPath(tooltipPath)
                            .styleBackground(style, isSystemInDarkTheme())
                            .styleBorderPath(style, tooltipPath, isSystemInDarkTheme())
                    ) {
                        content(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            containerPadding = style.getPaddings(),
                            safeAreaInsets = tooltipSettings.getContentPaddingValues()
                        )
                    }
                }
            }
        }
    }

    private fun Modifier.onTooltipSizeChanged(
        style: ComponentStyle?,
        containerDimens: MutableState<TooltipContainerDimens?>,
        contentDimens: MutableState<TooltipContentDimens?>,
        tooltipPointerPosition: TooltipPointerPosition,
        tooltipSettings: TooltipSettings,
        onTooltipSized: () -> Unit = {},
    ) = composed {
        val density = LocalDensity.current
        then(
            Modifier.onSizeChanged {
                with(density) {
                    containerDimens.value = TooltipContainerDimens(
                        widthDp = it.width.toDp(),
                        heightDp = it.height.toDp(),
                        widthPx = it.width.toFloat(),
                        heightPx = it.height.toFloat(),
                        cornerRadius = (style?.cornerRadius?.dp ?: 0.dp)
                    )

                    // find any pointer width to exclude from width/height in containerDimens
                    val pointerWidth = if (tooltipPointerPosition.isVertical) 0f else tooltipSettings.pointerLengthPx
                    val pointerHeight = if (tooltipPointerPosition.isVertical) tooltipSettings.pointerLengthPx else 0f

                    contentDimens.value = TooltipContentDimens(
                        widthDp = it.width.toDp() - pointerWidth.toDp(),
                        heightDp = it.height.toDp() - pointerHeight.toDp(),
                    )

                    onTooltipSized()
                }
            }
        )
    }

    private fun Modifier.clipToPath(path: Path) = then(
        Modifier.clip(GenericShape { _, _ -> addPath(path) })
    )

    private fun Modifier.styleBorderPath(
        style: ComponentStyle?,
        path: Path,
        isDark: Boolean
    ) = this.then(
        @Suppress("ComplexCondition")
        if (style?.borderWidth != null && style.borderWidth ne 0.0 && style.borderColor != null && !path.isEmpty) {
            Modifier
                .border(style.borderWidth.dp, style.borderColor.getColor(isDark), GenericShape { _, _ -> addPath(path) })
                .padding(style.borderWidth.dp)
        } else {
            Modifier
        }
    )

    private fun Modifier.positionTooltip(
        targetRect: Rect?,
        contentDimens: TooltipContentDimens?,
        pointerSettings: TooltipSettings,
        windowInfo: AppcuesWindowInfo,
        animationSpec: FiniteAnimationSpec<Dp>,
    ): Modifier = composed {
        // skip this until we have contentDimens defined
        if (contentDimens == null) return@composed Modifier

        Modifier.padding(
            start = calculatePaddingStart(windowInfo, contentDimens, pointerSettings, targetRect, animationSpec).value,
            top = calculatePaddingTop(windowInfo, contentDimens, pointerSettings, targetRect, animationSpec).value
        )
    }

    @Composable
    private fun calculatePaddingStart(
        windowInfo: AppcuesWindowInfo,
        contentDimens: TooltipContentDimens,
        pointerSettings: TooltipSettings,
        targetRect: Rect?,
        animationSpec: FiniteAnimationSpec<Dp>
    ): State<Dp> {
        val minPaddingStart = 0.dp
        val pointerLengthDp = with(LocalDensity.current) { pointerSettings.pointerLengthPx.toDp() }
        val pointerWidthDp = if (pointerSettings.tooltipPointerPosition.isVertical) 0.dp else pointerLengthDp
        val maxPaddingStart = (windowInfo.widthDp - contentDimens.widthDp - pointerWidthDp - (SCREEN_HORIZONTAL_PADDING * 2))
            .coerceAtLeast(minPaddingStart)

        return animateDpAsState(
            targetValue = when (pointerSettings.tooltipPointerPosition) {
                Left -> {
                    val targetReference = (targetRect?.right?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING
                    val padding = targetReference + pointerSettings.distance
                    // note: on horizontal, the max needs to account for the pointerLength as well
                    padding.coerceIn(minPaddingStart, maxPaddingStart)
                }
                Right -> {
                    val targetReference = (targetRect?.left?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING
                    // when pointing to the right - offset by the container width AND the pointer length to find the start
                    val padding = targetReference - pointerSettings.distance - contentDimens.widthDp - pointerLengthDp
                    // note: on horizontal, the max needs to account for the pointerLength as well
                    padding.coerceIn(minPaddingStart, maxPaddingStart)
                }
                None -> {
                    // in None case, the width is a fixed full width, 400 max
                    val containerMaxSize = windowInfo.widthDp - (SCREEN_HORIZONTAL_PADDING * 2)
                    val containerWidth = min(MAX_TOOLTIP_WIDTH, containerMaxSize)
                    val paddingStart = windowInfo.widthDp - containerWidth - (SCREEN_HORIZONTAL_PADDING * 2)
                    paddingStart.coerceAtLeast(minPaddingStart)
                }
                else -> {
                    val targetReference = (targetRect?.center?.x?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING - (contentDimens.widthDp / 2)

                    pointerSettings.pointerOffsetX.value = when {
                        targetReference < 0.dp -> targetReference
                        targetReference > maxPaddingStart -> targetReference - maxPaddingStart
                        else -> pointerSettings.pointerOffsetX.value
                    }

                    targetReference.coerceIn(minPaddingStart, maxPaddingStart)
                }
            },
            animationSpec = animationSpec
        )
    }

    @Composable
    private fun calculatePaddingTop(
        windowInfo: AppcuesWindowInfo,
        contentDimens: TooltipContentDimens,
        pointerSettings: TooltipSettings,
        targetRect: Rect?,
        animationSpec: FiniteAnimationSpec<Dp>
    ): State<Dp> {
        val minPaddingTop = 0.dp
        val pointerLengthDp = with(LocalDensity.current) { pointerSettings.pointerLengthPx.toDp() }
        val pointerHeightDp = if (pointerSettings.tooltipPointerPosition.isVertical) pointerLengthDp else 0.dp
        val maxPaddingTop = (windowInfo.heightDp - contentDimens.heightDp - pointerHeightDp - (SCREEN_VERTICAL_PADDING * 2))
            .coerceAtLeast(minPaddingTop)

        return animateDpAsState(
            targetValue = when (pointerSettings.tooltipPointerPosition) {
                Top -> {
                    val targetReference = (targetRect?.bottom?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING
                    val padding = targetReference + pointerSettings.distance
                    padding.coerceIn(minPaddingTop, maxPaddingTop)
                }
                Bottom -> {
                    val targetReference = (targetRect?.top?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING
                    val padding = targetReference - pointerSettings.distance - contentDimens.heightDp - pointerLengthDp
                    padding.coerceIn(minPaddingTop, maxPaddingTop)
                }
                None -> maxPaddingTop
                else -> {
                    val targetReference = (targetRect?.center?.y?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING - (contentDimens.heightDp / 2)

                    pointerSettings.pointerOffsetY.value = when {
                        targetReference < 0.dp -> targetReference
                        targetReference > maxPaddingTop -> targetReference - maxPaddingTop
                        else -> pointerSettings.pointerOffsetY.value
                    }

                    targetReference.coerceIn(minPaddingTop, maxPaddingTop)
                }
            },
            animationSpec = animationSpec
        )
    }

    private fun TooltipSettings.getContentPaddingValues(): PaddingValues {
        return when (tooltipPointerPosition) {
            Top -> PaddingValues(top = pointerLengthDp)
            Bottom -> PaddingValues(bottom = pointerLengthDp)
            Left -> PaddingValues(start = pointerLengthDp)
            Right -> PaddingValues(end = pointerLengthDp)
            None -> PaddingValues()
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

    private fun Modifier.tooltipSize(
        style: ComponentStyle?,
        windowInfo: AppcuesWindowInfo,
        tooltipSettings: TooltipSettings,
        useBottomToastStyle: Boolean,
    ): Modifier =
        composed {
            // keep containerMaxSize here as a rememberable so it force recomposition when value change
            val containerMaxSize = DpSize(
                width = windowInfo.widthDp - (SCREEN_HORIZONTAL_PADDING * 2),
                height = windowInfo.heightDp - (SCREEN_VERTICAL_PADDING * 2)
            )

            // Note: when pointer position is None (un-targeted), we use "bottom toast style". We fall back
            // to the max default width, capped at 400 for this bottom toast style presentation.
            val width = if (style?.width != null && !useBottomToastStyle) style.width.dp else MAX_TOOLTIP_WIDTH
            val pointerLengthDp = with(LocalDensity.current) { tooltipSettings.pointerLengthPx.toDp() }
            // add the pointerLength in case the tooltip has the pointer vertically positioned
            val requiredHeight = if (tooltipSettings.tooltipPointerPosition.isVertical) pointerLengthDp else 0.dp

            val modifier = when (tooltipSettings.tooltipPointerPosition) {
                Bottom, Top -> Modifier.width(min(width, containerMaxSize.width))
                Left, Right -> Modifier.width(min(width + pointerLengthDp, containerMaxSize.width))
                None -> Modifier.width(min(width, containerMaxSize.width))
            }.requiredHeightIn(48.dp + requiredHeight, containerMaxSize.height)

            return@composed modifier
        }

    @Composable
    private fun rememberTooltipPointerPosition(
        windowInfo: AppcuesWindowInfo,
        targetRectangleInfo: TargetRectangleInfo?,
        contentDimens: TooltipContentDimens?,
        targetRect: Rect?,
        distance: Dp,
        pointerLength: Dp,
        contentSized: Boolean,
    ): TooltipPointerPosition {
        // pointer position is only calculated once for a given target rectangle, after the container
        // is sized. The tooltip may end up getting a new container layout if it shifts the pointer
        // to another edge (not preferred edge) based on this calculation, but we stick with that
        // calculated position from that point forward and don't allow it to flip back
        return remember(targetRectangleInfo, contentSized) {
            targetRectangleInfo.getTooltipPointerPosition(
                windowInfo = windowInfo,
                contentDimens = contentDimens,
                targetRect = targetRect,
                distance = distance,
                pointerLength = pointerLength,
            )
        }
    }

    @Composable
    private fun rememberTooltipSettings(
        targetRectangleInfo: TargetRectangleInfo?,
        containerDimens: TooltipContainerDimens?,
        distance: Dp,
        position: TooltipPointerPosition,
    ): TooltipSettings {
        val density = LocalDensity.current
        return remember(targetRectangleInfo, containerDimens) {
            // gets the available space for the pointer based on position orientation
            val availableContainerSpace = if (position.isVertical) {
                containerDimens?.availableHorizontalSpace
            } else {
                containerDimens?.availableVerticalSpace
            } ?: 0.dp

            // 1. Remove any pointer related sizing params if this is an un-targeted tool tip, position == None
            // 2. limit the base to the available container space to avoid unwanted path shapes
            val pointerBasePx = if (position == None) 0f else with(density) { min(pointerBaseDp, availableContainerSpace).toPx() }
            val pointerLengthPx = if (position == None) 0f else with(density) { pointerLengthDp.toPx() }
            val pointerCornerRadiusPx = if (position == None) 0f else with(density) { pointerCornerRadius.toPx() }

            // calculate what is the width if we round the base corners (which increases the general base of the pointer)
            val roundedPointerWidth = calculateTooltipWidth(pointerBasePx, pointerLengthPx, pointerCornerRadiusPx)
            // only allow for rounding base corners if the width is still less than the available space
            val isRoundingBase = with(density) { roundedPointerWidth < availableContainerSpace.toPx() }

            TooltipSettings(
                tooltipPointerPosition = position,
                distance = distance,
                pointerBasePx = pointerBasePx,
                pointerLengthPx = pointerLengthPx,
                pointerCornerRadiusPx = pointerCornerRadiusPx,
                isRoundingBase = isRoundingBase
            )
        }
    }
}
