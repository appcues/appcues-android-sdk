package com.appcues.logging

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test

internal class LogcuesTest {

    private val logcues = Logcues(Dispatchers.Unconfined)

    @Test
    fun `info SHOULD emit info message`() = runBlocking {
        // WHEN
        logcues.info("message")
        // THEN
        logcues.messageFlow.test {
            val log = awaitItem()
            assertThat(log.type).isEqualTo(LogType.INFO)
            assertThat(log.message).isEqualTo("message")
        }
    }

    @Test
    fun `warning SHOULD emit warning message`() = runBlocking {
        // WHEN
        logcues.warning("message")
        // THEN
        logcues.messageFlow.test {
            val log = awaitItem()
            assertThat(log.type).isEqualTo(LogType.WARNING)
            assertThat(log.message).isEqualTo("message")
        }
    }

    @Test
    fun `debug SHOULD emit debug message`() = runBlocking {
        // WHEN
        logcues.debug("message")
        // THEN
        logcues.messageFlow.test {
            val log = awaitItem()
            assertThat(log.type).isEqualTo(LogType.DEBUG)
            assertThat(log.message).isEqualTo("message")
        }
    }

    @Test
    fun `error SHOULD emit info message`() = runBlocking {
        // WHEN
        logcues.error("message")
        // THEN
        logcues.messageFlow.test {
            val log = awaitItem()
            assertThat(log.type).isEqualTo(LogType.ERROR)
            assertThat(log.message).isEqualTo("message")
        }
    }

    @Test
    fun `error throwable SHOULD emit info message`() = runBlocking {
        // GIVEN
        val throwable = Throwable("message throwable")
        // WHEN
        logcues.error(throwable)
        // THEN
        logcues.messageFlow.test {
            val log = awaitItem()
            assertThat(log.type).isEqualTo(LogType.ERROR)
            assertThat(log.message).isEqualTo("message throwable")
        }
    }
}
