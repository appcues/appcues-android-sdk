package com.appcues.action

import com.appcues.AppcuesCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class DefaultActionQueue(
    private val coroutineScope: AppcuesCoroutineScope,
) : ActionQueue {

    override val queue: Channel<ExperienceAction> = Channel(Channel.UNLIMITED)

    override fun enqueue(actions: List<ExperienceAction>) {
        coroutineScope.launch { actions.forEach { queue.send(it) } }
    }
}
