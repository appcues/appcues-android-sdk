package com.appcues.trait.appcues

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
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
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.BOTTOM
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.NONE
import com.appcues.trait.appcues.TooltipTrait.PointerPosition.TOP
import com.appcues.ui.AppcuesOverlayViewManager
import com.appcues.ui.composables.AppcuesStepMetadata
import com.appcues.ui.composables.LocalAppcuesStepMetadata
import com.appcues.ui.composables.rememberAppcuesContentVisibility
import com.appcues.ui.extensions.coloredShadowPath
import com.appcues.ui.extensions.getColor
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

        private val SCREEN_HORIZONTAL_PADDING = 12.dp
        private val SCREEN_VERTICAL_PADDING = 24.dp
        private val MAX_WIDTH_DP = 350.dp
        private val MAX_HEIGHT_DP = 200.dp
    }

    private enum class PointerPosition {
        TOP, BOTTOM, NONE
    }

    private data class PointerSettings(
        val hidePointer: Boolean,
        val pointerPosition: PointerPosition,
        val pointerBase: Double,
        val pointerLength: Double
    ) {

        val pointerOffsetX = mutableStateOf(0.dp)
    }

    private val tooltipStyleSize = 20.dp

    private val style = config.getConfigStyle("style")

    private val preferredPosition = config.getConfigOrDefault("preferredPosition", "center")
    private val hidePointer = config.getConfigOrDefault("hidePointer", false)
    private val pointerBase = config.getConfigOrDefault("pointerBase", 12.0)
    private val pointerLength = config.getConfigOrDefault("pointerLength", 8.0)
    private val distanceFromTarget = config.getConfigOrDefault("distanceFromTarget", 0.0)

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).addView()
    }

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val metadata = LocalAppcuesStepMetadata.current
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current

        val windowInfo = rememberAppcuesWindowInfo()
        val targetRect = remember(metadata) { (metadata.actual[TargetElementTrait.METADATA_TARGET_RECT] as Rect?) }
        val pointerSettings = rememberPointerSettings(metadata, windowInfo, targetRect)
        val tooltipSizeDp = remember { mutableStateOf(DpSize(0.dp, 0.dp)) }
        // TODO change: remember? derivedState?
        val tooltipPath = tooltipPath(
            pointerSettings,
            tooltipSizeDp.value,
            style,
            layoutDirection,
            density,
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
                Column(modifier = Modifier.positionTooltip(targetRect, tooltipSizeDp.value, pointerSettings, windowInfo)) {
                    Box(
                        modifier = Modifier
                            .requiredSizeIn(maxWidth = MAX_WIDTH_DP, maxHeight = MAX_HEIGHT_DP)
                            .tooltipSize(style)
                            .onSizeChanged { with(density) { tooltipSizeDp.value = DpSize(it.width.toDp(), it.height.toDp()) } }
                            .styleShadowPath(style, tooltipPath, isSystemInDarkTheme())
                            .clipToPath(tooltipPath)
                            .styleBackground(style, isSystemInDarkTheme())
                            .styleBorderPath(style, tooltipPath, isSystemInDarkTheme())
                            .tooltipPointerPadding(pointerSettings)
                    ) {
                        content(false, PaddingValues(0.dp))
                    }
                }
            }
        }
    }

    @Composable
    private fun rememberPointerSettings(
        metadata: AppcuesStepMetadata,
        windowInfo: AppcuesWindowInfo,
        targetRect: Rect?
    ): PointerSettings {
        return remember(metadata) {
            val pointerPosition = when {
                targetRect == null -> NONE
                targetRect.center.y.dp < windowInfo.heightDp / 2 -> TOP
                else -> BOTTOM
            }

            PointerSettings(
                hidePointer = false,
                pointerPosition = pointerPosition,
                pointerBase = pointerBase,
                pointerLength = pointerLength
            )
        }
    }

    @Composable
    private fun tooltipPath(
        pointerSettings: PointerSettings,
        size: DpSize,
        style: ComponentStyle?,
        layoutDirection: LayoutDirection,
        density: Density
    ): Path {
        val containerSizePx = with(density) { Size(size.width.toPx(), size.height.toPx()) }
        val pointerSizePx = with(density) {
            Size(width = pointerSettings.pointerBase.dp.toPx(), height = pointerSettings.pointerLength.dp.toPx())
        }

        val containerOffsetPx = Offset(0f, if (pointerSettings.pointerPosition == TOP) pointerSizePx.height else 0f)
        val containerSizeWithPointer = containerSizePx.copy(height = containerSizePx.height - pointerSizePx.height)
        val cornerRadiusPx = with(density) { style?.cornerRadius?.dp?.toPx() ?: 0f }

        return Path().apply {
            val rect = Path().apply {
                if (style != null) {
                    addPath(
                        Path().apply {
                            addOutline(
                                RoundedCornerShape(style.cornerRadius.dp).createOutline(
                                    containerSizeWithPointer,
                                    layoutDirection,
                                    density
                                )
                            )
                        },
                        containerOffsetPx
                    )
                } else {
                    addRect(Rect(containerOffsetPx, containerSizeWithPointer))
                }
            }

            val base = pointerSizePx.width
            val baseCenter = base / 2
            val topSizePointer = animateFloatAsState(
                targetValue = when (pointerSettings.pointerPosition) {
                    TOP -> -pointerSizePx.height
                    else -> 0f
                }
            )
            val bottomSizePointer = animateFloatAsState(
                targetValue = when (pointerSettings.pointerPosition) {
                    BOTTOM -> cornerRadiusPx + pointerSizePx.height
                    else -> 0f
                }
            )
            val offsetX = with(density) { (size.width.toPx() / 2) - baseCenter + pointerSettings.pointerOffsetX.value.toPx() }
            val minPointerOffset = 0f
            val maxPointerOffset = with(density) { size.width.toPx() - base }
            // TODO change so this value changes based on how much offset is needed for the pointer to point at target element
            val pointerOffsetX = if (offsetX < minPointerOffset) {
                (offsetX - minPointerOffset).coerceAtLeast(-baseCenter)
            } else if (offsetX > maxPointerOffset) {
                (offsetX - maxPointerOffset).coerceAtMost(baseCenter)
            } else 0f

            val offsetXAnimated = animateFloatAsState(
                targetValue = offsetX.coerceAtLeast(minPointerOffset).coerceAtMost(maxPointerOffset)
            )
            val offsetY = animateFloatAsState(
                targetValue = if (pointerSettings.pointerPosition == BOTTOM)
                    containerSizeWithPointer.height - cornerRadiusPx else -topSizePointer.value
            )
            val pointer = Path().apply {
                reset()

                if (pointerSettings.pointerPosition == TOP) {
                    lineTo(x = base / 2 + pointerOffsetX, y = topSizePointer.value)
                }
                lineTo(x = base, y = 0f)
                lineTo(x = base, y = cornerRadiusPx)

                if (pointerSettings.pointerPosition == BOTTOM) {
                    lineTo(x = base / 2 + pointerOffsetX, y = bottomSizePointer.value)
                }
                lineTo(x = 0f, y = cornerRadiusPx)
                lineTo(x = 0f, y = 0f)

                close()

                translate(Offset(offsetXAnimated.value, offsetY.value))
            }

            op(pointer, rect, PathOperation.Union)
        }
    }

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
        tooltipSizeDp: DpSize,
        pointerSettings: PointerSettings,
        windowInfo: AppcuesWindowInfo
    ): Modifier = composed {

        val tooltipPaddingTop = animateDpAsState(
            targetValue = when (pointerSettings.pointerPosition) {
                TOP -> max((targetRect?.bottom?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING, 0.dp)
                BOTTOM -> max((targetRect?.top?.dp ?: 0.dp) - tooltipSizeDp.height - tooltipStyleSize - SCREEN_VERTICAL_PADDING, 0.dp)
                NONE -> windowInfo.heightDp - tooltipSizeDp.height - SCREEN_VERTICAL_PADDING
            }
        )

        val toolTipPaddingStart = animateDpAsState(
            targetValue = if (targetRect != null) {
                val paddingStartMin = 0.dp
                val paddingStartMax = windowInfo.widthDp - (SCREEN_HORIZONTAL_PADDING * 2) - tooltipSizeDp.width
                val paddingStartOnTarget = targetRect.center.x.dp - SCREEN_HORIZONTAL_PADDING - (tooltipSizeDp.width / 2)

                if (paddingStartOnTarget < 0.dp) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget
                } else if (paddingStartOnTarget > paddingStartMax) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget - paddingStartMax
                }
                // target value is between min and max
                paddingStartOnTarget.coerceIn(paddingStartMin, paddingStartMax)
            } else {
                // When no targetRect is found we align the tooltip at bottomCenter of the screen
                val paddingStartCentered = (windowInfo.widthDp - tooltipSizeDp.width - (SCREEN_HORIZONTAL_PADDING * 2)) / 2
                // Min value
                paddingStartCentered.coerceAtLeast(0.dp)
            }
        )

        then(Modifier.padding(start = toolTipPaddingStart.value, top = tooltipPaddingTop.value))
    }

    private fun Modifier.tooltipPointerPadding(pointerSettings: PointerSettings): Modifier {
        return then(
            when (pointerSettings.pointerPosition) {
                TOP -> Modifier.padding(top = pointerSettings.pointerLength.dp)
                BOTTOM -> Modifier
                NONE -> Modifier
            }
        )
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
}
