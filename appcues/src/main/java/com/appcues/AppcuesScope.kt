package com.appcues

import com.appcues.data.AppcuesRepository
import com.appcues.logging.Logcues
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class AppcuesScope(
    private val logcues: Logcues,
    private val repository: AppcuesRepository,
    private val stateMachine: StateMachine
) : CoroutineScope {

    private val parentJob = SupervisorJob()

    private val dispatcher = Dispatchers.Main

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logcues.error(Exception(throwable))
    }

    override val coroutineContext = parentJob + dispatcher + exceptionHandler

    init {
        launch {
            stateMachine.flow.collect {
                logcues.info("StateMachine moved to state -> ${it::class.simpleName}")
            }
        }
    }

    fun show(contentId: String) {
        // should this check if the state is Idling before even trying to fetch
        // the experience? since it cannot show anyway, if already in another state?
        launch {
            logcues.info("show(contentId: $contentId)")
            repository.getContent(contentId).also {
                stateMachine.handleAction(StartExperience(it))
            }
        }
    }
}
