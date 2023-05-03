package com.appcues.action

import com.appcues.data.model.AppcuesConfigMap

/**
 * A type that describes an action that can be triggered from an Experience.
 */
public interface ExperienceAction {

    /**
     * Configuration options for this action.
     */
    public val config: AppcuesConfigMap

    /**
     * Execute the action.
     */
    public suspend fun execute()
}
