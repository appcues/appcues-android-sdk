package com.appcues.trait

/**
 * AppcuesPaginationData is used to communicate between different traits
 * information regarding pagination.
 *
 * Every [ContentHolderTrait] should update this information if other
 * traits are supposed to react to stuff like page changes, horizontal scrolling
 * between pages, etc..
 */
data class AppcuesPaginationData(
    val pageCount: Int,
    val currentPage: Int,
    val scrollOffset: Float
)
