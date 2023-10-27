package com.appcues.debugger

import com.appcues.logging.LogMessage
import com.appcues.logging.Logcues
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

internal class DebuggerLogMessageManager(private val logcues: Logcues) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    private val messages: ArrayList<LogMessage> = arrayListOf()

    private val _data = MutableStateFlow<List<LogMessage>>(arrayListOf())
    val data: StateFlow<List<LogMessage>>
        get() = _data

    private var isStarted = false
    fun start() {
        if (isStarted) return

        launch {
            logcues.messageFlow.collect { onMessage(it) }
        }

        isStarted = true
    }

    fun reset() {
        coroutineContext.cancelChildren()
        isStarted = false
        messages.clear()

        launch { updateData() }
    }

    private suspend fun onMessage(logMessage: LogMessage) {
        messages.add(0, logMessage)

        updateData()
    }

    suspend fun updateData() {
        _data.emit(messages)
    }
}
