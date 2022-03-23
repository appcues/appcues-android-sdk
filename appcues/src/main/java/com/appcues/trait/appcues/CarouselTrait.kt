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
import com.appcues.ui.LocalAppcuesPagination
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collect

internal class CarouselTrait(
    override val config: HashMap<String, Any>?,
) : ContentHolderTrait {

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BoxScope.CreateContentHolder(pages: List<@Composable () -> Unit>, pageIndex: Int) {
        val pagerState = rememberPagerState()
        val localPagination = LocalAppcuesPagination.current
        // state machine changed the page, so we animate to that page
        LaunchedEffect(pageIndex) {
            pagerState.animateScrollToPage(pageIndex)
        }

        // we scrolled over to next page, so we notify the local pagination listener
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }
                .collect { localPagination.onPageChanged(pagerState.currentPage) }
        }

        HorizontalPager(
            // change
            modifier = Modifier.animateContentSize(),
            count = pages.size,
            state = pagerState,
            verticalAlignment = Alignment.Top
        ) { index ->
            // Our page content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                pages[index]()
            }
        }
    }
}
