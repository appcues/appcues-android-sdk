package com.appcues.action

import com.appcues.Appcues
import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.appcues.StepInteractionAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.data.model.RenderContext
import com.appcues.logging.Logcues
import com.appcues.mocks.mockExperience
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.State
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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

@OptIn(ExperimentalCoroutinesApi::class)
internal class ActionProcessorTest : KoinTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @After
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `process SHOULD transform queue WHEN it contains a transformation action`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 0, true)
        val scope = initScope(initialState)
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

    @Test
    fun `process(1,2,3) SHOULD send stepInteraction analytics based on last of type MetadataSettingsAction`() = runTest {

        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 0, true)
        val scope = initScope(initialState)
        val experienceRenderer = scope.get<ExperienceRenderer>()
        val actionProcessor = scope.get<ActionProcessor>()
        val analyticsTracker: AnalyticsTracker = scope.get()

        experienceRenderer.show(experience)

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
            RenderContext.Modal,
            listOf(action1, stepAction1, action2, stepAction2, action3, action4, stepAction3, action5),
            BUTTON_TAPPED,
            "Button 1"
        )

        // THEN
        val properties = slot<Map<String, Any>>()
        verify { analyticsTracker.track("appcues:v2:step_interaction", capture(properties), false, true) }
        with(properties.captured["interactionData"] as Map<*, *>) {
            assertThat(this["destination"]).isEqualTo("c")
            assertThat(this["text"]).isEqualTo("Button 1")
            assertThat(this["category"]).isEqualTo("test")
        }
    }

    @Test
    fun `unpublished experience SHOULD NOT send stepInteraction analytics`() = runTest {

        // GIVEN
        val experience = mockExperience().copy(published = false)
        val initialState = RenderingStep(experience, 0, true)
        val scope = initScope(initialState)
        val actionProcessor = scope.get<ActionProcessor>()
        val analyticsTracker: AnalyticsTracker = scope.get()
        val stepAction1 = TestStepInteractionAction("test", "a")

        // WHEN
        actionProcessor.process(
            RenderContext.Modal,
            listOf(stepAction1),
            BUTTON_TAPPED,
            "Button 1"
        )

        // THEN
        verify { analyticsTracker wasNot Called }
    }

    // Helpers
    private fun initScope(initState: State): Scope {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()
        val scopeId = UUID.randomUUID().toString()
        return startKoin {
            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
                        scoped { scope }
                        scoped { mockk<Logcues>(relaxed = true) }
                        scoped { mockk<Appcues>(relaxed = true) }
                        scoped { AppcuesConfig("00000", "123") }
                        scoped { AppcuesCoroutineScope(get()) }
                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
                        scoped { ActionProcessor(get()) }
                        scoped { params ->
                            StepInteractionAction(
                                renderContext = RenderContext.Modal,
                                interaction = params[1],
                                analyticsTracker = get(),
                                experienceRenderer = get(),
                            )
                        }
                        factory { StateMachine(get(), get(), get(), initState) }
                        scoped { ExperienceRenderer(scope) }
                        scoped { RenderContext.Modal }
                        scoped { mockk<ExperienceLifecycleTracker>(relaxed = true) }
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

        override suspend fun execute() {
            executeCount++
        }
    }

    private class TestStepInteractionAction(
        override val category: String,
        override val destination: String
    ) : ExperienceAction, MetadataSettingsAction {

        override val config: AppcuesConfigMap
            get() = emptyMap()

        override suspend fun execute() {
            // do nothing
        }
    }
}
