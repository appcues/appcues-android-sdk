package com.appcues.logging

import com.appcues.logging.LogType.DEBUG
import com.appcues.logging.LogType.ERROR
import com.appcues.logging.LogType.INFO
import com.appcues.logging.LogType.WARNING
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.coroutines.CoroutineContext

internal class Logcues(private val dispatcher: CoroutineDispatcher = Dispatchers.Default) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = dispatcher

    private val _messageFlow = MutableSharedFlow<LogMessage>(replay = 15)
    val messageFlow: SharedFlow<LogMessage> = _messageFlow

    fun info(message: String) {
        launch { _messageFlow.emit(LogMessage(message, INFO, Date())) }
    }

    fun warning(message: String) {
        launch { _messageFlow.emit(LogMessage(message, WARNING, Date())) }
    }

    fun debug(message: String) {
        launch { _messageFlow.emit(LogMessage(message, DEBUG, Date())) }
    }

    fun error(message: String) {
        launch { _messageFlow.emit(LogMessage(message, ERROR, Date())) }
    }

    fun error(throwable: Throwable) {
        error(throwable.message.toString())
    }
}
