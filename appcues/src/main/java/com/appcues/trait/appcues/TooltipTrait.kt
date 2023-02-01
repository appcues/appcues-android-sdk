package com.appcues.trait.appcues

import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
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
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_IN_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.EASE_OUT
import com.appcues.trait.appcues.StepAnimationTrait.StepAnimationEasing.LINEAR
import com.appcues.trait.appcues.TargetRectangleTrait.TargetRectangleInfo
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_END
import com.appcues.trait.appcues.TooltipPointerPosition.BOTTOM_START
import com.appcues.trait.appcues.TooltipPointerPosition.NONE
import com.appcues.trait.appcues.TooltipPointerPosition.TOP
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_END
import com.appcues.trait.appcues.TooltipPointerPosition.TOP_START
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
        private const val POINTER_BASE_DEFAULT = 12.0
        private const val POINTER_LENGTH_DEFAULT = 8.0
    }

    private val style = config.getConfigStyle("style")

    private val pointerBaseDp = config.getConfigOrDefault("pointerBase", POINTER_BASE_DEFAULT).dp
    private val pointerLengthDp = config.getConfigOrDefault("pointerLength", POINTER_LENGTH_DEFAULT).dp
    // implementation remaining properties
    // private val preferredPosition = config.getConfigOrDefault("preferredPosition", "center")
    // private val hidePointer = config.getConfigOrDefault("hidePointer", false)

    override fun present() {
        AppcuesOverlayViewManager(scope = scope).start()
    }

    @Composable
    override fun WrapContent(content: @Composable (hasFixedHeight: Boolean, contentPadding: PaddingValues?) -> Unit) {
        val density = LocalDensity.current
        val metadata = LocalAppcuesStepMetadata.current

        val windowInfo = rememberAppcuesWindowInfo()
        val targetRect = rememberMetadataRect(metadata)

        val floatAnimation = rememberMetadataFloatAnimation(metadata)
        val dpAnimation = rememberMetadataDpAnimation(metadata)

        val containerDimens = remember { mutableStateOf<TooltipContainerDimens?>(null) }

        val tooltipSettings =
            remember(targetRect, containerDimens.value) {
                getTooltipSettings(
                    density,
                    getPointerPosition(windowInfo, targetRect),
                    pointerBaseDp,
                    pointerLengthDp,
                )
            }

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
                Column(modifier = Modifier.positionTooltip(targetRect, containerDimens.value, tooltipSettings, windowInfo, dpAnimation)) {
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
                            .tooltipPointerPadding(tooltipSettings)
                    ) {
                        content(false, PaddingValues(0.dp))
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
                TOP, TOP_START, TOP_END -> max((targetRect?.bottom?.dp ?: 0.dp) - SCREEN_VERTICAL_PADDING, 0.dp)
                BOTTOM, BOTTOM_START, BOTTOM_END -> max(
                    (targetRect?.top?.dp ?: 0.dp) - containerDimens.heightDp - SCREEN_VERTICAL_PADDING, 0.dp
                )
                NONE -> windowInfo.heightDp - containerDimens.heightDp - SCREEN_VERTICAL_PADDING
            },
            animationSpec = animationSpec
        )

        val toolTipPaddingStart = animateDpAsState(
            targetValue = if (targetRect != null) {
                val paddingStartMin = 0.dp
                val paddingStartMax = windowInfo.widthDp - (SCREEN_HORIZONTAL_PADDING * 2) - containerDimens.widthDp
                val paddingStartOnTarget = targetRect.center.x.dp - SCREEN_HORIZONTAL_PADDING - (containerDimens.widthDp / 2)

                if (paddingStartOnTarget < 0.dp) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget
                } else if (paddingStartOnTarget > paddingStartMax) {
                    pointerSettings.pointerOffsetX.value = paddingStartOnTarget - paddingStartMax
                }

                // target value is between min and max
                paddingStartOnTarget.coerceIn(paddingStartMin, paddingStartMax)
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

    private fun Modifier.tooltipPointerPadding(pointerSettings: TooltipSettings): Modifier {
        return then(
            when (pointerSettings.pointerPosition) {
                TOP, TOP_START, TOP_END -> Modifier.padding(top = pointerSettings.pointerLengthDp)
                BOTTOM, BOTTOM_START, BOTTOM_END -> Modifier.padding(bottom = pointerSettings.pointerLengthDp)
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

    @Composable
    private fun rememberMetadataFloatAnimation(metadata: AppcuesStepMetadata): TweenSpec<Float> {
        return remember(metadata) {
            val duration = (metadata.actual[StepAnimationTrait.METADATA_ANIMATION_DURATION] as Int?) ?: StepAnimationTrait.DEFAULT_ANIMATION
            when ((metadata.actual[StepAnimationTrait.METADATA_ANIMATION_EASING] as StepAnimationEasing?)) {
                LINEAR -> tween(durationMillis = duration, easing = LinearEasing)
                EASE_IN -> tween(durationMillis = duration, easing = EaseIn)
                EASE_OUT -> tween(durationMillis = duration, easing = EaseOut)
                EASE_IN_OUT -> tween(durationMillis = duration, easing = EaseInOut)
                // animation with no duration is the easiest way to not use animation here
                null -> tween(durationMillis = 0, easing = LinearEasing)
            }
        }
    }

    @Composable
    private fun rememberMetadataDpAnimation(metadata: AppcuesStepMetadata): FiniteAnimationSpec<Dp> {
        return remember(metadata) {
            val duration = (metadata.actual[StepAnimationTrait.METADATA_ANIMATION_DURATION] as Int?) ?: StepAnimationTrait.DEFAULT_ANIMATION
            when ((metadata.actual[StepAnimationTrait.METADATA_ANIMATION_EASING] as StepAnimationEasing?)) {
                LINEAR -> tween(durationMillis = duration, easing = LinearEasing)
                EASE_IN -> tween(durationMillis = duration, easing = EaseIn)
                EASE_OUT -> tween(durationMillis = duration, easing = EaseOut)
                EASE_IN_OUT -> tween(durationMillis = duration, easing = EaseInOut)
                // animation with no duration is the easiest way to not use animation here
                null -> tween(durationMillis = 0, easing = LinearEasing)
            }
        }
    }

    @Composable
    private fun rememberMetadataRect(metadata: AppcuesStepMetadata): Rect? {
        val windowInfo = rememberAppcuesWindowInfo()
        return remember(metadata) {
            val rectInfo = (metadata.actual[TargetRectangleTrait.TARGET_RECTANGLE_METADATA] as TargetRectangleInfo?)
            val screenWidth = windowInfo.widthDp.value
            val screenHeight = windowInfo.heightDp.value

            if (rectInfo == null) return@remember null

            Rect(
                offset = Offset(
                    x = (screenWidth * rectInfo.relativeX).toFloat() + rectInfo.x,
                    y = (screenHeight * rectInfo.relativeY).toFloat() + rectInfo.y,
                ),
                size = Size(
                    width = (screenWidth * rectInfo.relativeWidth).toFloat() + rectInfo.width,
                    height = (screenHeight * rectInfo.relativeHeight).toFloat() + rectInfo.height,
                )
            )
        }
    }
}
