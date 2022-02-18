package com.appcues

import android.util.Log
import com.appcues.data.AppcuesRepository
import com.appcues.experience.container.DialogModalPresenter
import com.appcues.logging.Logcues
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal class AppcuesScope(
    private val logcues: Logcues,
    private val repository: AppcuesRepository,
    private val presenter: DialogModalPresenter
) : CoroutineScope {

    private val parentJob = SupervisorJob()

    override val coroutineContext = parentJob + Dispatchers.Main + CoroutineExceptionHandler { _, error ->
        Log.i("Appcues", "AppcuesScope error handler -> exception: $error")
    }

    fun show(contentId: String) {
        launch {
            logcues.info("show(contentId: $contentId)")
            presenter.show(repository.getContent(contentId))
        }
    }
}
