package com.appcues.trait.appcues

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.appcues.trait.ContentHolderTrait
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.ui.AppcuesPaginationData
import com.appcues.ui.LocalAppcuesPagination
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collect

internal class CarouselTrait(
    override val config: HashMap<String, Any>?,
) : ContentHolderTrait {

    companion object {
        const val TYPE = "@appcues/carousel"
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BoxScope.CreateContentHolder(containerPages: ContainerPages) {
        val pagerState = rememberPagerState().also {
            containerPages.setPaginationData(
                AppcuesPaginationData(
                    pageCount = it.pageCount,
                    currentPage = it.currentPage,
                    scrollOffset = it.currentPageOffset
                )
            )
        }

        val localPagination = LocalAppcuesPagination.current

        // state machine changed the page, so we animate to that page
        LaunchedEffect(containerPages.pageIndex) {
            pagerState.animateScrollToPage(containerPages.pageIndex)
        }

        // we scrolled over to next page, so we notify the local pagination listener
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { localPagination.onPageChanged(it) }
        }

        HorizontalPager(
            // change
            modifier = Modifier.animateContentSize(),
            count = containerPages.pages.size,
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            // Our page content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                containerPages.pages[index]()
            }
        }
    }
}
