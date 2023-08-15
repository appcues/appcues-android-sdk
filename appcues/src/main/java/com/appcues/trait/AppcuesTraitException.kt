package com.appcues.trait

/**
 * A type of exception that can occur during the application of an ExperienceTrait.
 */
internal class AppcuesTraitException(
    message: String,

    /**
     *  When set, this value can indicate a number of milliseconds after which re-application
     *  of traits can be attempted, to try to auto-recover from this error.
     */
    val retryMilliseconds: Int? = null,

) : Exception(message)
