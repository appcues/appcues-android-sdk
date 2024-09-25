package com.appcues.trait.appcues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.ui.composables.AppcuesPaginationData
import com.appcues.ui.composables.LocalAppcuesPaginationDelegate
import kotlinx.coroutines.flow.drop

internal class CarouselTrait(
    override val config: Map<String, Any>?,
) : ContentHolderTrait {

    companion object {

        const val TYPE = "@appcues/carousel"
    }

    @Composable
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) {
        val pagerState = rememberPagerState(
            initialPage = containerPages.currentPage,
            pageCount = { containerPages.pageCount }
        ).also {
            containerPages.UpdatePaginationData(
                AppcuesPaginationData(
                    pageCount = containerPages.pageCount,
                    currentPage = it.currentPage,
                    scrollOffset = it.currentPageOffsetFraction
                )
            )
        }

        val localPagination = LocalAppcuesPaginationDelegate.current

        // keep track if programmatically scrolling to a page, so we don't raise
        // page change events for intermediate pages during the scroll
        val scrollingToPage = remember { mutableStateOf<Int?>(null) }

        // state machine changed the page, so we animate to that page
        LaunchedEffect(containerPages.currentPage) {
            // only trigger the scroll if we are not already on the desired page
            if (pagerState.currentPage != containerPages.currentPage) {
                // store this value so we can guard against excess page change events below
                scrollingToPage.value = containerPages.currentPage
                pagerState.animateScrollToPage(containerPages.currentPage)
            }
        }

        // we scrolled over to next page, so we notify the local pagination listener
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect {
                    if (scrollingToPage.value != null) {
                        // if we've reached the destination of a programmatic scroll, clear this value
                        // and re-enable page change events for user interaction.
                        // otherwise, leave value as-is, as we are still processing a programmatic scroll
                        scrollingToPage.value = if (it == scrollingToPage.value) null else scrollingToPage.value
                    } else {
                        // normal user interaction has changed the page, update listener
                        localPagination.onPageChanged(it)
                    }
                }
        }

        // this is a workaround a problem that was found with this HorizontalPager
        // where sometimes the currentPageOffset number is bigger or smaller than the expected range of {-1.0f, 1.0f}
        LaunchedEffect(pagerState.currentPageOffsetFraction) {
            snapshotFlow { pagerState.currentPageOffsetFraction }
                .drop(1)
                .collect {
                    when {
                        it < -1.0f -> pagerState.scrollToPage(pagerState.currentPage - 1)
                        it > 1.0f -> pagerState.scrollToPage(pagerState.currentPage + 1)
                    }
                }
        }

        val horizontalPagerSize = remember { HorizontalPagerSize(pagerState) }

        HorizontalPager(
            modifier = Modifier
                // basically we are measuring the pager layout and if we do have a valid containerHeight we use onGloballyPositioned
                // else we will go with the measured height from the HorizontalPager until we can calculate the container properly
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    val containerHeight = horizontalPagerSize.getContainerHeight()
                    val calculatedHeight = if (containerHeight.isNaN()) placeable.height else containerHeight.toInt()

                    layout(constraints.maxWidth, calculatedHeight) { placeable.place(0, 0) }
                },
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            Box(
                // gets the size of each page and store it in a remembered state
                modifier = Modifier.onSizeChanged { horizontalPagerSize.onHeightChanged(index, it.height) }
            ) {
                containerPages.composePage(index)
            }
        }
    }

    private class HorizontalPagerSize(private val pagerState: PagerState) {

        val heightMap = mutableStateMapOf<Int, Int>()

        fun getContainerHeight(): Float {
            val currentPageSize = heightMap[pagerState.currentPage] ?: 0
            val nextPageSize = when {
                pagerState.currentPageOffsetFraction > 0 -> heightMap[pagerState.currentPage + 1] ?: 0
                pagerState.currentPageOffsetFraction < 0 -> heightMap[pagerState.currentPage - 1] ?: 0
                else -> currentPageSize
            }

            val offsetNormalized = if (pagerState.currentPageOffsetFraction < 0)
                pagerState.currentPageOffsetFraction * -1 else pagerState.currentPageOffsetFraction

            val pageSize = (nextPageSize - currentPageSize) * offsetNormalized + currentPageSize

            // if pageSize is nan or zero we return it so does not restrict
            // the size to whatever value is in containerSize
            return if (pageSize.isNaN() || pageSize == 0f) Float.NaN
            // when we do know whats the size of the page and the container size then we
            // use whatever is bigger value
            else pageSize
        }

        fun onHeightChanged(index: Int, height: Int) {
            heightMap[index] = height
        }
    }
}
