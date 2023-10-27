package com.appcues.logging

import android.util.Log
import com.appcues.LoggingLevel.DEBUG
import com.appcues.LoggingLevel.INFO
import com.appcues.LoggingLevel.NONE
import io.mockk.called
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date

internal class LogcatDestinationTest {

    private val logcues = mockk<Logcues>()

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 1
        every { Log.d(any(), any()) } returns 1
        every { Log.e(any(), any()) } returns 1
    }

    @Test
    fun `info SHOULD log messages WHEN level is INFO`() = runTest {
        // GIVEN
        val sharedFlow = MutableSharedFlow<LogMessage>()
        coEvery { logcues.messageFlow } returns sharedFlow
        LogcatDestination(logcues, INFO, Dispatchers.Unconfined).apply { init() }

        // WHEN
        sharedFlow.emit(LogMessage("message1", LogType.INFO, Date()))
        sharedFlow.emit(LogMessage("message2", LogType.ERROR, Date()))
        sharedFlow.emit(LogMessage("message3", LogType.DEBUG, Date()))
        // THEN
        verifySequence {
            Log.i("Appcues", "message1")
            Log.e("Appcues", "message2")
        }
    }

    @Test
    fun `info SHOULD log nothing WHEN level is NONE`() = runTest {
        // GIVEN
        val sharedFlow = MutableSharedFlow<LogMessage>()
        coEvery { logcues.messageFlow } returns sharedFlow
        LogcatDestination(logcues, NONE, Dispatchers.Unconfined).apply { init() }

        // WHEN
        sharedFlow.emit(LogMessage("message1", LogType.INFO, Date()))
        sharedFlow.emit(LogMessage("message2", LogType.ERROR, Date()))
        sharedFlow.emit(LogMessage("message3", LogType.DEBUG, Date()))
        // THEN
        verify { Log::class.java wasNot called }
    }

    @Test
    fun `info SHOULD log messages WHEN level is DEBUG`() = runTest {
        // GIVEN
        val sharedFlow = MutableSharedFlow<LogMessage>()
        coEvery { logcues.messageFlow } returns sharedFlow
        LogcatDestination(logcues, DEBUG, Dispatchers.Unconfined).apply { init() }

        // WHEN
        sharedFlow.emit(LogMessage("message1", LogType.INFO, Date()))
        sharedFlow.emit(LogMessage("message2", LogType.ERROR, Date()))
        sharedFlow.emit(LogMessage("message3", LogType.DEBUG, Date()))
        // THEN
        verifySequence {
            Log.i("Appcues", "message1")
            Log.e("Appcues", "message2")
            Log.d("Appcues", "message3")
        }
    }
}
