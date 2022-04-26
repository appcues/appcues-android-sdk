package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class ActionProcessor(
    private val appcues: Appcues,
    private val appcuesCoroutineScope: AppcuesCoroutineScope,
) {

    private val actionQueue = Channel<ExperienceAction>(Channel.UNLIMITED)

    init {
        appcuesCoroutineScope.launch {
            for (action in actionQueue) {
                action.execute(appcues)
            }
        }
    }

    fun process(action: ExperienceAction) {
        appcuesCoroutineScope.launch {
            actionQueue.send(action)
        }
    }
}
