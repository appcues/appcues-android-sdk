package com.appcues.action

import com.appcues.Appcues
import com.appcues.data.model.AppcuesConfigMap

/**
 * A type that describes an action that can be triggered from an Experience.
 */
interface ExperienceAction {

    /**
     * Configuration options for this action.
     */
    val config: AppcuesConfigMap

    /**
     * Execute the action.
     *
     * @param appcues The Appcues instance displaying the experience triggering the action.
     */
    suspend fun execute(appcues: Appcues)
}
