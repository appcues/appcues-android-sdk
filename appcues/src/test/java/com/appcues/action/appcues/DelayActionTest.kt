package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.rules.TestScopeRule
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@ExperimentalTime
internal class DelayActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Test
    fun `close SHOULD have expected type name`() {
        assertThat(DelayAction.TYPE).isEqualTo("@appcues/delay")
    }

    @Test
    fun `delay SHOULD call delay with given value of 300ms `() = runTest {
        // GIVEN
        val action = DelayAction(mapOf("duration" to 300))

        launch {
            val workDuration = testScheduler.timeSource.measureTime {
                // WHEN
                action.execute()
            }
            assertThat(workDuration).isEqualTo(300.milliseconds)
        }
    }

    @Test
    fun `delay SHOULD call delay with given value of 1000ms `() = runTest {
        // GIVEN
        val action = DelayAction(mapOf("duration" to 1000))

        launch {
            val workDuration = testScheduler.timeSource.measureTime {
                // WHEN
                action.execute()
            }
            assertThat(workDuration).isEqualTo(1000.milliseconds)
        }
    }
}
