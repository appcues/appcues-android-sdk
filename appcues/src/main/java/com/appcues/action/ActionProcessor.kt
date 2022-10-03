package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope

// Responsible for queueing up any ExperienceActions to run in sequential order,
// to allow for things like closing current experience + launching a new experience, for example.
internal class ActionProcessor(override val scope: Scope) : KoinScopeComponent {

    // lazy initialization injection to avoid circular dependency
    private val appcues: Appcues by inject()
    private val appcuesCoroutineScope: AppcuesCoroutineScope by inject()

    private val actionQueue = Channel<ExperienceAction>(Channel.UNLIMITED)

    init {
        appcuesCoroutineScope.launch {
            for (action in actionQueue) {
                action.execute(appcues)
            }
        }
    }

    fun process(actions: List<ExperienceAction>) {
        appcuesCoroutineScope.launch {
            actions.forEach {
                actionQueue.send(it)
            }
        }
    }
}
