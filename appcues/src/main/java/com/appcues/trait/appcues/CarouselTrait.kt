package com.appcues.trait.appcues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.ui.AppcuesPaginationData
import com.appcues.ui.LocalAppcuesPaginationDelegate
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

internal class CarouselTrait(
    override val config: Map<String, Any>?,
) : ContentHolderTrait {

    companion object {

        const val TYPE = "@appcues/carousel"
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) {
        val pagerState = rememberPagerState(containerPages.currentPage).also {
            containerPages.setPaginationData(
                AppcuesPaginationData(
                    pageCount = it.pageCount,
                    currentPage = it.currentPage,
                    scrollOffset = it.currentPageOffset
                )
            )
        }

        val localPagination = LocalAppcuesPaginationDelegate.current

        // state machine changed the page, so we animate to that page
        LaunchedEffect(containerPages.currentPage) {
            pagerState.animateScrollToPage(containerPages.currentPage)
        }

        // we scrolled over to next page, so we notify the local pagination listener
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { localPagination.onPageChanged(it) }
        }

        val horizontalPagerSize = remember { HorizontalPagerSize(pagerState) }

        HorizontalPager(
            modifier = Modifier.size(with(LocalDensity.current) { horizontalPagerSize.pagerContainerSize.toDp() }),
            count = containerPages.pageCount,
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

    @ExperimentalPagerApi
    private class HorizontalPagerSize constructor(private val pagerState: PagerState) {

        val heightMap = mutableStateMapOf<Int, Int>()

        val pagerContainerSize
            get() = run {
                val currentPageSize = heightMap[pagerState.currentPage] ?: 0
                val nextPageSize = when {
                    pagerState.currentPageOffset > 0 -> heightMap[pagerState.currentPage + 1] ?: 0
                    pagerState.currentPageOffset < 0 -> heightMap[pagerState.currentPage - 1] ?: 0
                    else -> currentPageSize
                }

                val offsetNormalized = if (pagerState.currentPageOffset < 0)
                    pagerState.currentPageOffset * -1
                else
                    pagerState.currentPageOffset

                (nextPageSize - currentPageSize) * offsetNormalized + currentPageSize
            }

        fun onHeightChanged(index: Int, height: Int) {
            if (height != 0 && heightMap[index] ?: 0 == 0) {
                heightMap[index] = height
            }
        }
    }
}
