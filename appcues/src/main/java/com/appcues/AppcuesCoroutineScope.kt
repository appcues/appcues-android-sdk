package com.appcues

import com.appcues.logging.Logcues
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

internal class AppcuesCoroutineScope(
    logcues: Logcues
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = SupervisorJob() +
        Dispatchers.Main +
        CoroutineExceptionHandler { _, throwable -> logcues.error(Exception(throwable)) }
}
