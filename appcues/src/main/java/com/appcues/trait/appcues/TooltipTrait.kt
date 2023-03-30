package com.appcues.trait.appcues

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.requiredWidthIn
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
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
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
    override fun WrapContent(
        content: @Composable (modifier: Modifier, containerPadding: PaddingValues, safeAreaInsets: PaddingValues) -> Unit
    ) {
        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val targetRectInfo = rememberTargetRectangleInfo(metadata)
        val windowInfo = rememberAppcuesWindowInfo()
        val containerDimens = remember { mutableStateOf<TooltipContainerDimens?>(null) }
        val targetRect = targetRectInfo.getRect(windowInfo)
        val distance = targetRectInfo.getContentDistance()
        // keep tooltipMaxHeight here as a rememberable so it force recomposition when value change
        val tooltipMaxHeight = remember(targetRectInfo) { mutableStateOf(windowInfo.heightDp - (SCREEN_VERTICAL_PADDING * 2)) }

        val floatAnimation = rememberFloatStepAnimation(metadata)
        val dpAnimation = rememberDpStepAnimation(metadata)

        val tooltipSettings = rememberTooltipSettings(
            windowInfo = windowInfo,
            targetRectangleInfo = targetRectInfo,
            containerDimens = containerDimens.value,
            targetRect = targetRect,
            distance = distance,
            tooltipMaxHeight = tooltipMaxHeight,
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
                        targetRect = targetRect,
                        containerDimens = containerDimens.value,
                        pointerSettings = tooltipSettings,
                        windowInfo = windowInfo,
                        animationSpec = dpAnimation
                    )
                ) {
                    val tooltipPath = tooltipPath(tooltipSettings, containerDimens.value, floatAnimation, dpAnimation)
                    val maxWidth = windowInfo.widthDp - SCREEN_HORIZONTAL_PADDING * 2

                    Box(
                        modifier = Modifier
                            .tooltipSize(style, maxWidth, tooltipMaxHeight.value)
                            .onTooltipSizeChanged(style, density, containerDimens)
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
        density: Density,
        containerDimens: MutableState<TooltipContainerDimens?>
    ) = then(
        Modifier.onSizeChanged {
            with(density) {
                containerDimens.value = TooltipContainerDimens(
                    widthDp = it.width.toDp(),
                    heightDp = it.height.toDp(),
                    widthPx = it.width.toFloat(),
                    heightPx = it.height.toFloat(),
                    cornerRadius = (style?.cornerRadius?.dp ?: 0.dp)
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
        containerDimens: TooltipContainerDimens?,
        pointerSettings: TooltipSettings,
        windowInfo: AppcuesWindowInfo,
        animationSpec: FiniteAnimationSpec<Dp>,
    ): Modifier = composed {
        // skip this until we have containerDimens defined
        if (containerDimens == null) return@composed Modifier

        Modifier.padding(
            start = calculatePaddingStart(windowInfo, containerDimens, pointerSettings, targetRect, animationSpec).value,
            top = calculatePaddingTop(windowInfo, containerDimens, pointerSettings, targetRect, animationSpec).value
        )
    }

    @Composable
    private fun calculatePaddingStart(
        windowInfo: AppcuesWindowInfo,
        containerDimens: TooltipContainerDimens,
        pointerSettings: TooltipSettings,
        targetRect: Rect?,
        animationSpec: FiniteAnimationSpec<Dp>
    ): State<Dp> {
        val minPaddingStart = 0.dp
        val maxPaddingStart = (windowInfo.widthDp - containerDimens.widthDp - (SCREEN_HORIZONTAL_PADDING * 2)).coerceAtLeast(0.dp)

        return animateDpAsState(
            targetValue = when (pointerSettings.tooltipPointerPosition) {
                Left -> {
                    val targetReference = (targetRect?.right?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING
                    val padding = targetReference + pointerSettings.distance
                    padding.coerceIn(minPaddingStart, maxPaddingStart)
                }
                Right -> {
                    val targetReference = (targetRect?.left?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING
                    val padding = targetReference - pointerSettings.distance - containerDimens.widthDp
                    padding.coerceIn(minPaddingStart, maxPaddingStart)
                }
                None -> {
                    val paddingStartCentered = (windowInfo.widthDp - containerDimens.widthDp - (SCREEN_HORIZONTAL_PADDING * 2)) / 2
                    paddingStartCentered.coerceAtLeast(minPaddingStart)
                }
                else -> {
                    val targetReference = (targetRect?.center?.x?.dp ?: 0.dp) - SCREEN_HORIZONTAL_PADDING - (containerDimens.widthDp / 2)

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
        containerDimens: TooltipContainerDimens,
        pointerSettings: TooltipSettings,
        targetRect: Rect?,
        animationSpec: FiniteAnimationSpec<Dp>
    ): State<Dp> {
        val minPaddingTop = 0.dp
        val maxPaddingTop = (windowInfo.heightDp - containerDimens.heightDp - (SCREEN_VERTICAL_PADDING * 2)).coerceAtLeast(0.dp)
        return animateDpAsState(
            targetValue = when (pointerSettings.tooltipPointerPosition) {
                Top -> {
                    val targetReference = (targetRect?.bottom?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING
                    val padding = targetReference + pointerSettings.distance
                    padding.coerceIn(minPaddingTop, maxPaddingTop)
                }
                Bottom -> {
                    val targetReference = (targetRect?.top?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING
                    val padding = targetReference - pointerSettings.distance - containerDimens.heightDp
                    padding.coerceIn(minPaddingTop, maxPaddingTop)
                }
                None -> maxPaddingTop
                else -> {
                    val targetReference = (targetRect?.center?.y?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING - (containerDimens.heightDp / 2)

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

    private fun Modifier.tooltipSize(style: ComponentStyle?, maxWidth: Dp, maxHeight: Dp): Modifier = then(
        if (style?.width == null) Modifier.requiredWidthIn(
            min = 48.dp,
            max = min(400.dp, maxWidth)
        ) else Modifier.width(width = style.width.dp)
    ).then(
        if (style?.height == null) Modifier.requiredHeightIn(
            min = 48.dp,
            max = max(48.dp, maxHeight)
        ) else Modifier.height(height = style.height.dp)
    )

    @Composable
    private fun rememberTooltipSettings(
        windowInfo: AppcuesWindowInfo,
        targetRectangleInfo: TargetRectangleInfo?,
        containerDimens: TooltipContainerDimens?,
        targetRect: Rect?,
        distance: Dp,
        tooltipMaxHeight: MutableState<Dp>,
    ): TooltipSettings {
        val density = LocalDensity.current
        return remember(targetRectangleInfo, containerDimens) {
            val pointerPosition = targetRectangleInfo.getTooltipPointerPosition(
                windowInfo = windowInfo,
                containerDimens = containerDimens,
                targetRect = targetRect,
                tooltipMaxHeight = tooltipMaxHeight
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
