package com.appcues

import java.util.UUID

/**
 * A listener that informs about Experience content being rendered inside of the application.
 */
public interface ExperienceListener {
    /**
     * Notifies the listener when an Experience is starting.
     *
     * @param experienceId The ID of the Experience that is starting.
     */
    public fun experienceStarted(experienceId: UUID)

    /**
     * Notifies the listener when an Experience has finished.
     *
     * @param experienceId The ID of the Experience that has finished.
     */
    public fun experienceFinished(experienceId: UUID)
}
