package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.statemachine.State.Idling

internal class StateMachineFactory(
    private val coroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
    private val actionProcessor: ActionProcessor,
    private val lifecycleTracker: ExperienceLifecycleTracker,
) {

    fun create(state: State = Idling): StateMachine = StateMachine(coroutineScope, config, actionProcessor, lifecycleTracker, state)
}
