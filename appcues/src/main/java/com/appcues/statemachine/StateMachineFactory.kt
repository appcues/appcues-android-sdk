package com.appcues.statemachine

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.analytics.EventTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.model.Experience
import com.appcues.statemachine.State.Idling

internal class StateMachineFactory(
    private val coroutineScope: AppcuesCoroutineScope,
    private val config: AppcuesConfig,
    private val actionProcessor: ActionProcessor,
) {

    fun create(eventTracker: EventTracker, state: State = Idling, onEndedExperience: ((Experience) -> Unit)? = null): StateMachine {
        val experienceLifecycleTracker = ExperienceLifecycleTracker(coroutineScope, eventTracker)
        return StateMachine(coroutineScope, config, actionProcessor, experienceLifecycleTracker, onEndedExperience, state)
    }
}
