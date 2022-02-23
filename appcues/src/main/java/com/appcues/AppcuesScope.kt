package com.appcues

import android.util.Log
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

    override val coroutineContext = parentJob + Dispatchers.Main + CoroutineExceptionHandler { _, error ->
        Log.i("Appcues", "AppcuesScope error handler -> exception: $error")
    }

    init {
        launch {
            stateMachine.flow.collect {
                logcues.info("moved to state $it")
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
