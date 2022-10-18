package com.appcues.action

import kotlinx.coroutines.channels.Channel

/**
 * Interface that represents the queue for actionProcessor
 *
 * this interface facilitates testing which ExperienceActions are
 * being processed in the queue.
 */
internal interface ActionQueue {

    val queue: Channel<ExperienceAction>

    fun enqueue(actions: List<ExperienceAction>)
}
