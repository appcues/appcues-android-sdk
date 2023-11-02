package com.appcues.trait

/**
 * A type of exception that can occur during the application of an ExperienceTrait.
 */
internal class AppcuesTraitException(
    override val message: String,

    /**
     *  When set, this value can indicate a number of milliseconds after which re-application
     *  of traits can be attempted, to try to auto-recover from this error.
     */
    val retryMilliseconds: Int? = null,

    /**
     * When true, this means that the issue may be recoverable at a later time, for instance
     * if a target element is not found, but future layout updates to scroll other content
     * into view could resolve the target.
     */
    val recoverable: Boolean = false
) : Exception(message)
