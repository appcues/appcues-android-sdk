package com.appcues.trait

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.appcues.ui.composables.AppcuesPaginationData
import com.appcues.ui.composables.appcuesPaginationData

/**
 * A trait that can hold one or more pages of content and control the paging behavior to navigate
 * between them.
 */
public interface ContentHolderTrait : ExperienceTrait {

    /**
     * Defines the content holder, usually used to support different kinds of pagination between steps
     * that belong inside the same step group
     *
     * Example usage:
     * @sample com.appcues.trait.appcues.CarouselTrait
     *
     * @param containerPages current page information
     */
    @Composable
    public fun BoxScope.CreateContentHolder(containerPages: ContainerPages)

    /**
     * Class used as a parameter for [CreateContentHolder]
     */
    public data class ContainerPages(
        /**
         * The total number of pages in this container.
         */
        val pageCount: Int,

        /**
         * The index of the current page.
         */
        val currentPage: Int,

        /**
         * A function that produces the Composable for the given page index.
         */
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
        public fun setPaginationData(paginationData: AppcuesPaginationData) {
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
