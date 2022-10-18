package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.logging.Logcues
import com.appcues.rules.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
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
        val actionProcessor = ActionProcessor(scope, scope.get())
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

    @Test
    fun `process(1,2,3) SHOULD send stepInteraction analytics based on last of type MetadataSettingsAction`() = runBlocking {
        // GIVEN
        val scope = initScope()
        val actionQueue = TestActionQueue()
        val actionProcessor = ActionProcessor(scope, actionQueue)

        val action1 = TestAction(false)
        val stepAction1 = TestStepInteractionAction("test", "a")
        val action2 = TestAction(false)
        val stepAction2 = TestStepInteractionAction("test", "b")
        val action3 = TestAction(false)
        val action4 = TestAction(false)
        val stepAction3 = TestStepInteractionAction("test", "c")
        val action5 = TestAction(false)

        // WHEN
        actionProcessor.process(
            listOf(action1, stepAction1, action2, stepAction2, action3, action4, stepAction3, action5),
            StepInteraction.InteractionType.BUTTON_TAPPED,
            "Button 1"
        )

        // THEN
        assertThat(actionQueue.listQueue).hasSize(9)
        assertThat(actionQueue.listQueue.first()).isInstanceOf(StepInteractionAction::class.java)
        with(actionQueue.listQueue.first() as StepInteractionAction) {
            assertThat(interaction.properties["destination"]).isEqualTo("c")
            assertThat(interaction.properties["category"]).isEqualTo("test")
            assertThat(interaction.properties["text"]).isEqualTo("Button 1")
        }
    }

    // Helpers
    private fun initScope(): Scope {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()
        val scopeId = UUID.randomUUID().toString()
        return startKoin {
            koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
                        scoped { mockk<Logcues>(relaxed = true) }
                        scoped { mockk<Appcues>(relaxed = true) }
                        scoped<ActionQueue> { DefaultActionQueue(get()) }
                        scoped { AppcuesCoroutineScope(get()) }
                        scoped { params ->
                            StepInteractionAction(
                                config = params.getOrNull(),
                                interaction = params.get(),
                                analyticsTracker = mockk(relaxed = true),
                                stateMachine = mockk(relaxed = true),
                            )
                        }
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

    private class TestStepInteractionAction(
        override val category: String,
        override val destination: String
    ) : ExperienceAction, MetadataSettingsAction {

        override val config: AppcuesConfigMap
            get() = emptyMap()

        override suspend fun execute(appcues: Appcues) {
            // do nothing
        }
    }

    private class TestActionQueue : ActionQueue {

        val listQueue = arrayListOf<ExperienceAction>()

        override val queue: Channel<ExperienceAction>
            get() = Channel(Channel.UNLIMITED)

        override fun enqueue(actions: List<ExperienceAction>) {
            listQueue.addAll(actions)
        }
    }
}
