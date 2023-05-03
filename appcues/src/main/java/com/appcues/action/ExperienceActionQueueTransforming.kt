package com.appcues.action

import com.appcues.Appcues

/**
 * An `ExperienceAction` that performs modifications of the action queue executed following an interaction in an experience.
 */
internal interface ExperienceActionQueueTransforming : ExperienceAction {

    /**
     *  Modify the queue of actions executed in an experience.
     *
     * @param queue The current queue of actions.
     * @param index The index of the current action in the `queue`.
     * @param appcues The `Appcues` instance that displayed the experience triggering the action.
     *
     * @return The updated queue.
     */
    fun transformQueue(queue: List<ExperienceAction>, index: Int, appcues: Appcues): List<ExperienceAction>
}
