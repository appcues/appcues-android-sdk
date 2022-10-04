package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import org.koin.test.KoinTest
import java.util.UUID

class ActionProcessorTest : KoinTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @After
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `process SHOULD transform queue WHEN it contains a transformation action`() {
        // GIVEN
        val scope = initScope()
        val actionProcessor = ActionProcessor(scope)
        val action1 = TestAction(false)
        val action2 = TestAction(false)
        val action3 = TestAction(true)
        val action4 = TestAction(false)
        val action5 = TestAction(false)

        // WHEN
        actionProcessor.process(listOf(action1, action2, action3, action4, action5))

        // THEN

        // the last two actions should be removed
        assertThat(TestAction.executeCount).isEqualTo(3)
    }

    // Helpers
    private fun initScope(): Scope {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()
        val scopeId = UUID.randomUUID().toString()
        return startKoin {
            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
                        scoped { mockk<Logcues>(relaxed = true) }
                        scoped { mockk<Appcues>(relaxed = true) }
                        scoped { AppcuesCoroutineScope(get()) }
                    }
                }
            )
        }.koin.getScope(scopeId)
    }

    private class TestAction(private val removeSubsequent: Boolean) : ExperienceActionQueueTransforming {

        companion object {
            var executeCount: Int = 0
        }

        override fun transformQueue(queue: List<ExperienceAction>, index: Int, appcues: Appcues): List<ExperienceAction> {
            if (removeSubsequent) {
                return queue.subList(0, index + 1)
            }
            return queue
        }

        override val config: AppcuesConfigMap
            get() = emptyMap()

        override suspend fun execute(appcues: Appcues) {
            executeCount++
        }
    }
}
