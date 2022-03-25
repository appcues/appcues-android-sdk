package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.ui.AppcuesPaginationData
import com.appcues.ui.appcuesPaginationData

interface ContentHolderTrait : ExperienceTrait {

    @Composable
    fun BoxScope.CreateContentHolder(containerPages: ContainerPages)

    /**
     * Class used as a parameter for [CreateContentHolder]
     *
     * [pages] the page compositions
     * [pageIndex] current page index
     */
    data class ContainerPages(
        val pages: List<@Composable () -> Unit>,
        val pageIndex: Int,
    ) {

        private var defaultPaginationData: AppcuesPaginationData = AppcuesPaginationData(
            pageCount = pages.size,
            currentPage = pageIndex,
            scrollOffset = 0.0f,
        )

        private var customPaginationData: AppcuesPaginationData? = null

        /**
         * set the pagination data based on custom [CreateContentHolder] implementation
         */
        fun setPaginationData(paginationData: AppcuesPaginationData) {
            appcuesPaginationData.value = paginationData
            customPaginationData = paginationData
        }

        /**
         * the SDK will invoke this function to ensure we have the minimum
         * required [AppcuesPaginationData] that can be used by other traits
         * in case no [setPaginationData] was called during [CreateContentHolder]
         */
        internal fun syncPaginationData() {
            if (customPaginationData == null) {
                appcuesPaginationData.value = defaultPaginationData
            }
        }
    }
}
