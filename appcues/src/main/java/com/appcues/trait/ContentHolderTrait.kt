package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.ui.composables.AppcuesPaginationData
import com.appcues.ui.composables.appcuesPaginationData

interface ContentHolderTrait : ExperienceTrait {

    /**
     * Defines the content holder, usually used to support different kinds of pagination between steps
     * that belong inside the same step group
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.CarouselTrait
     *
     * @param countainerPages current page information
     */
    @Composable
    fun BoxScope.CreateContentHolder(containerPages: ContainerPages)

    /**
     * Class used as a parameter for [CreateContentHolder]
     *
     * [pages] the page compositions
     * [currentPage] current page index
     */
    data class ContainerPages(
        val pageCount: Int,
        val currentPage: Int,
        val composePage: @Composable (index: Int) -> Unit,
    ) {

        private var defaultPaginationData: AppcuesPaginationData = AppcuesPaginationData(
            pageCount = pageCount,
            currentPage = currentPage,
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
