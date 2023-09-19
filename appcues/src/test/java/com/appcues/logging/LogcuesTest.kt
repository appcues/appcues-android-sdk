package com.appcues.logging

import android.util.Log
import com.appcues.LoggingLevel.INFO
import com.appcues.LoggingLevel.NONE
import io.mockk.called
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class LogcuesTest {

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i("Appcues", "message") } returns 1
        every { Log.e("Appcues", "message") } returns 1
    }

    @Test
    fun `info SHOULD log message WHEN level is INFO`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = INFO)
        // WHEN
        logcues.info("message")
        // THEN
        verify { Log.i("Appcues", "message") }
    }

    @Test
    fun `info SHOULD not log WHEN level is NONE`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = NONE)
        // WHEN
        logcues.info("message")
        // THEN
        verify { Log::class.java wasNot called }
    }

    @Test
    fun `error SHOULD log message WHEN level is INFO`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = INFO)
        // WHEN
        logcues.error("message")
        // THEN
        verify { Log.e("Appcues", "message") }
    }

    @Test
    fun `error SHOULD not log WHEN level is NONE`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = NONE)
        // WHEN
        logcues.error("message")
        // THEN
        verify { Log::class.java wasNot called }
    }

    @Test
    fun `error SHOULD log throwable message WHEN level is INFO`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = INFO)
        // WHEN
        logcues.error(Throwable("message"))
        // THEN
        verify { Log.e("Appcues", "message") }
    }

    @Test
    fun `error SHOULD not log throwable WHEN level is NONE`() {
        // GIVEN
        val logcues = Logcues(loggingLevel = NONE)
        // WHEN
        logcues.error(Throwable("message"))
        // THEN
        verify { Log::class.java wasNot called }
    }
}
