package com.appcues

import com.appcues.logging.Logcues
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

internal class AppcuesCoroutineScope(
    logcues: Logcues,
    dispatcher: CoroutineDispatcher = Dispatchers.Main
) : CoroutineScope {

    override val coroutineContext: CoroutineContext =
        SupervisorJob() +
            dispatcher +
            CoroutineExceptionHandler { _, throwable -> logcues.error(Exception(throwable)) }
}
