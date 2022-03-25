package com.appcues

import java.util.UUID

/**
 * Provides a set of listener methods that can be used to be informed about when Experience
 * content is being rendered inside of the application.
 */
interface ExperienceListener {
    /**
     * Notifies the listener when an Experience is starting.
     *
     * [experienceId] The ID of the Experience that is starting.
     */
    fun experienceStarted(experienceId: UUID)

    /**
     * Notifies the listener when an Experience has finished.
     *
     * [experienceId] The ID of the Experience that has finished.
     */
    fun experienceFinished(experienceId: UUID)
}
