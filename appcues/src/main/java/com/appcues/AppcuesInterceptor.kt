package com.appcues

import java.util.UUID

/**
 * An interceptor that can optionally be applied during initialization
 * to control Appcues behaviors at runtime.
 */
interface AppcuesInterceptor {

    /**
     * Determines if the given Appcues experience can display.  Can be used to handle
     * additional processing needed to get the application into a valid state to display
     * before returning a value.
     *
     * @param experienceId the ID of the experience that is being requested to display.
     * @return true if the experience can be shown, false if not.
     */
    suspend fun canDisplayExperience(experienceId: UUID): Boolean
}
