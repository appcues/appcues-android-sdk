package com.appcues.trait.appcues

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.getConfigStyle
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.ui.composables.rememberAppcuesPaginationState
import com.appcues.ui.extensions.getBoxAlignment
import com.appcues.ui.extensions.getColor

internal class PagingDotsTrait(
    override val config: AppcuesConfigMap
) : ContainerDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/paging-dots"
        private const val DEFAULT_PADDING = 8.0
    }

    override val containerComposeOrder = ContainerDecoratingType.OVERLAY

    private val style = config.getConfigStyle("style")

    @Composable
    override fun BoxScope.DecorateContainer() {
        val paginationData = rememberAppcuesPaginationState()
        val pageCount = paginationData.value.pageCount

        if (pageCount < 2) return

        val activeColor: Color =
            style?.foregroundColor.getColor(isSystemInDarkTheme()) ?: LocalContentColor.current.copy(alpha = LocalContentAlpha.current)
        val inactiveColor: Color =
            style?.backgroundColor.getColor(isSystemInDarkTheme()) ?: activeColor.copy(ContentAlpha.disabled)

        val indicatorWidth: Dp = 8.dp
        val indicatorHeight: Dp = indicatorWidth
        val spacing: Dp = indicatorWidth

        val indicatorShape: Shape = CircleShape
        val indicatorWidthPx = LocalDensity.current.run { indicatorWidth.roundToPx() }
        val spacingPx = LocalDensity.current.run { spacing.roundToPx() }

        val currentPage = paginationData.value.currentPage
        val paginationOffset = paginationData.value.scrollOffset

        Box(
            modifier = Modifier
                .align(style.getBoxAlignment())
                .padding(
                    (DEFAULT_PADDING + (style?.marginLeading ?: 0.0)).dp,
                    (DEFAULT_PADDING + (style?.marginTop ?: 0.0)).dp,
                    (DEFAULT_PADDING + (style?.marginTrailing ?: 0.0)).dp,
                    (DEFAULT_PADDING + (style?.marginBottom ?: 0.0)).dp
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val indicatorModifier = Modifier
                    .size(width = indicatorWidth, height = indicatorHeight)
                    .background(color = inactiveColor, shape = indicatorShape)

                repeat(pageCount) {
                    Box(indicatorModifier)
                }
            }

            Box(
                Modifier
                    .offset {
                        val maxScrollPosition = (pageCount - 1)
                            .coerceAtLeast(0)
                            .toFloat()

                        val scrollPosition = (currentPage + paginationOffset)
                            .coerceIn(0f, maxScrollPosition)

                        IntOffset(
                            x = ((spacingPx + indicatorWidthPx) * scrollPosition).toInt(),
                            y = 0
                        )
                    }
                    .size(width = indicatorWidth, height = indicatorHeight)
                    .background(
                        color = activeColor,
                        shape = indicatorShape,
                    )
            )
        }
    }
}
