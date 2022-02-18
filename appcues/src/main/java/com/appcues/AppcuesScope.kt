package com.appcues

import android.util.Log
import com.appcues.data.AppcuesRepository
import com.appcues.logging.Logcues
import com.appcues.statemachine.StateMachine
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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

    fun show(contentId: String) {
        launch {
            logcues.info("show(contentId: $contentId)")
            stateMachine.showExperience(repository.getContent(contentId))
        }
    }
}
