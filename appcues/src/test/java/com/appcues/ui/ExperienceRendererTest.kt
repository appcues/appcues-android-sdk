package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.AppcuesCoroutineScope
import com.appcues.AppcuesFrameView
import com.appcues.AppcuesInterceptor
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.analytics.track
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.Experiment
import com.appcues.data.model.QualificationResult
import com.appcues.data.model.RenderContext
import com.appcues.data.model.RenderContext.Embed
import com.appcues.data.model.RenderContext.Modal
import com.appcues.di.AppcuesModule
import com.appcues.di.Bootstrap
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.di.scope.get
import com.appcues.logging.Logcues
import com.appcues.mocks.mockEmbedExperience
import com.appcues.mocks.mockExperience
import com.appcues.mocks.mockExperienceExperiment
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.states.IdlingState
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.ui.ExperienceRenderer.RenderingResult
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.UUID

internal class ExperienceRendererTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is on last step`() = runTest {
        // GIVEN
        val state = RenderingStepState(mockExperience(), 3, mutableMapOf())
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val experience = mockk<Experience>(relaxed = true) {
            every { this@mockk.renderContext } answers { RenderContext.Modal }
        }
        experienceRenderer.show(experience)
        // WHEN
        experienceRenderer.dismiss(RenderContext.Modal, markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is not on last step`() = runTest {
        // GIVEN
        val state = RenderingStepState(mockExperience(), 2, mutableMapOf())
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val experience = mockk<Experience>(relaxed = true) {
            every { this@mockk.renderContext } answers { RenderContext.Modal }
        }
        experienceRenderer.show(experience)

        // WHEN
        experienceRenderer.dismiss(RenderContext.Modal, markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `show SHOULD NOT show experience WHEN an experiment is active AND group is control`() = runTest {
        // GIVEN
        val experiment = Experiment(
            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
            group = "control",
            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
            goalId = "my-goal",
            contentType = "my-content-type"
        )
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        val showResult = experienceRenderer.show(experience)

        // THEN
        assertThat(showResult).isInstanceOf(RenderingResult.WontDisplay::class.java)
        coVerify { stateMachine.handleAction(StartExperience(experience)) wasNot Called }
    }

    @Test
    fun `show SHOULD show experience WHEN an experiment is active AND group is exposed`() = runTest {
        // GIVEN
        val experiment = Experiment(
            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
            group = "exposed",
            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
            goalId = "my-goal",
            contentType = "my-content-type"
        )
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        val showResult = experienceRenderer.show(experience)

        // THEN
        assertThat(showResult).isInstanceOf(RenderingResult.Success::class.java)
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `show SHOULD track experiment_entered with group=control WHEN an experiment is active AND group is control`() = runTest {
        // GIVEN
        val experiment = Experiment(
            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
            group = "control",
            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
            goalId = "my-goal",
            contentType = "my-content-type"
        )
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.show(experience)

        // THEN
        verify { analyticsTracker.track(experiment) }
    }

    @Test
    fun `show SHOULD track experiment_entered with group=exposed WHEN an experiment is active AND group is exposed`() = runTest {
        // GIVEN
        val experiment = Experiment(
            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
            group = "exposed",
            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
            goalId = "my-goal",
            contentType = "my-content-type"
        )
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.show(experience)

        // THEN
        verify { analyticsTracker.track(experiment) }
    }

    // Embeds
    @Test
    fun `qualify SHOULD start experience WHEN frame already exists`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val context = RenderContext.Embed("frame1")
        experienceRenderer.start(mockk(relaxed = true), context)

        // WHEN
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))

        // THEN
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `qualify SHOULD start experience WHEN a matching frame is registered`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)
        val context = RenderContext.Embed("frame1")
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))

        // WHEN
        experienceRenderer.start(mockk(relaxed = true), context)

        // THEN
        val slot = slot<Map<String, Any>>()
        verify {
            analyticsTracker.track(
                AnalyticsEvent.ExperienceError,
                capture(slot),
                false
            )
        }
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
        val errorId = slot.captured["errorId"] as String
        verify {
            analyticsTracker.track(
                AnalyticsEvent.ExperienceRecovery,
                mapOf(
                    "errorId" to errorId,
                ),
                false
            )
        }
    }

    @Test
    fun `non screen view triggers SHOULD NOT clear the embed cache`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val context = RenderContext.Embed("frame1")
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))

        // WHEN
        // all of these will process through but the embed will remain in cache and still start after this
        experienceRenderer.show(QualificationResult(Qualification("event_trigger"), listOf()))
        experienceRenderer.show(QualificationResult(Qualification("unknown_value"), listOf()))

        experienceRenderer.start(mockk(relaxed = true), context)

        // THEN
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `screen view triggers SHOULD clear the embed cache`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val context = RenderContext.Embed("frame1")
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))

        // WHEN
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf()))
        experienceRenderer.start(mockk(relaxed = true), context)

        // THEN
        coVerify { stateMachine.handleAction(StartExperience(experience)) wasNot Called }
    }

    // tests a recycler view like case, where the same frame gets re-registered
    @Test
    fun `frame re-register SHOULD restart the experience again WHEN frame retainContent is true`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val directory: StateMachineDirectory = scope.get()
        val context = RenderContext.Embed("frame1")
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))
        val frameMock = mockk<AppcuesFrameView>(relaxed = true) {
            every { this@mockk.retainContent } answers { true }
        }

        // WHEN
        experienceRenderer.start(frameMock, context)
        experienceRenderer.start(frameMock, context)

        // THEN
        val owner = directory.getOwner(context)
        val ownerMachine = owner!!.stateMachine
        coVerify(exactly = 1) { ownerMachine.stop(false) }
        coVerify(exactly = 2) { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `frame re-register SHOULD NOT restart the experience again WHEN frame retainContent is false`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val directory: StateMachineDirectory = scope.get()
        val context = RenderContext.Embed("frame1")
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))
        val frameMock = mockk<AppcuesFrameView>(relaxed = true) {
            every { this@mockk.retainContent } answers { false }
        }

        // WHEN
        experienceRenderer.start(frameMock, context)
        experienceRenderer.start(frameMock, context)

        // THEN
        val owner = directory.getOwner(context)
        val ownerMachine = owner!!.stateMachine
        coVerify(exactly = 1) { ownerMachine.stop(false) }
        coVerify(exactly = 1) { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `frame register to new context SHOULD reset the previous context`() = runTest {
        // GIVEN
        val experience = mockEmbedExperience("frame1")
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { IdlingState }
            coEvery { this@mockk.handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val directory: StateMachineDirectory = scope.get()
        val context1 = RenderContext.Embed("frame1")
        val context2 = RenderContext.Embed("frame2")
        val frame: AppcuesFrameView = mockk(relaxed = true)
        experienceRenderer.show(QualificationResult(Qualification("screen_view"), listOf(experience)))

        // WHEN
        experienceRenderer.start(frame, context1)
        val owner1 = directory.getOwner(context1)
        experienceRenderer.start(frame, context2)
        val owner2 = directory.getOwner(context2)

        // THEN

        val owner1Machine = owner1!!.stateMachine
        val owner2Machine = owner2!!.stateMachine
        coVerify(exactly = 1) { owner1Machine.handleAction(StartExperience(experience)) }
        coVerify(exactly = 1) { owner1Machine.stop(false) }
        coVerify(exactly = 1) { owner2Machine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `show SHOULD dismiss current experience WHEN new experience is normal priority`() = runTest {
        // GIVEN
        val existingExperience = mockk<Experience>(relaxed = true) {
            every { renderContext } answers { RenderContext.Modal }
            every { priority } answers { NORMAL }
        }
        val newExperience = mockk<Experience>(relaxed = true) {
            every { renderContext } answers { RenderContext.Modal }
            every { priority } answers { NORMAL }
        }
        var state: State = RenderingStepState(existingExperience, 0, mutableMapOf())
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { handleAction(EndExperience(markComplete = false, destroyed = false)) } answers {
                state = IdlingState
                Success(state)
            }
            coEvery { handleAction(StartExperience(newExperience)) } answers {
                state = RenderingStepState(newExperience, 0, mutableMapOf())
                Success(state)
            }
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.show(newExperience)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `show SHOULD NOT dismiss current experience WHEN it is the same instance ID`() = runTest {
        // GIVEN
        val experience = mockk<Experience>(relaxed = true) {
            every { renderContext } answers { RenderContext.Modal }
            every { priority } answers { NORMAL }
        }
        val state: State = RenderingStepState(experience, 0, mutableMapOf())
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { handleAction(any()) } returns Success(IdlingState)
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        val result = experienceRenderer.show(experience)

        // THEN
        // it will be considered a successful render, since already showing, but nothing will change in the state machine
        assertThat(result).isEqualTo(RenderingResult.Success)
        coVerify(exactly = 0) { stateMachine.handleAction(StartExperience(experience)) }
        coVerify(exactly = 0) { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `show SHOULD NOT render WHEN config interceptor blocks an experience ID`() = runTest {
        // GIVEN
        val mockExperienceId = UUID.randomUUID()
        val experience = mockk<Experience>(relaxed = true) {
            every { id } returns mockExperienceId
        }
        val stateMachine = mockk<StateMachine>(relaxed = true)
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val config: AppcuesConfig = scope.get()
        config.interceptor = object : AppcuesInterceptor {
            override suspend fun canDisplayExperience(experienceId: UUID): Boolean {
                return experienceId != mockExperienceId
            }
        }

        // WHEN
        val result = experienceRenderer.show(experience)

        // THEN
        assertThat(result).isEqualTo(RenderingResult.WontDisplay)
        coVerify(exactly = 0) { stateMachine.handleAction(StartExperience(experience)) }
        coVerify(exactly = 0) { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `show SHOULD render WHEN config interceptor allows an experience ID`() = runTest {
        // GIVEN
        val mockExperienceId = UUID.randomUUID()
        val experience = mockk<Experience>(relaxed = true) {
            every { id } returns mockExperienceId
            every { renderContext } returns RenderContext.Modal
        }
        var state: State = IdlingState
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { handleAction(StartExperience(experience)) } answers {
                state = RenderingStepState(experience, 0, mutableMapOf())
                Success(state)
            }
        }
        val scope = createScope { stateMachine }
        val experienceRenderer = ExperienceRenderer(scope)
        val config: AppcuesConfig = scope.get()
        config.interceptor = object : AppcuesInterceptor {
            override suspend fun canDisplayExperience(experienceId: UUID): Boolean {
                return experienceId == mockExperienceId
            }
        }

        // WHEN
        val result = experienceRenderer.show(experience)

        // THEN
        assertThat(result).isEqualTo(RenderingResult.Success)
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `onViewConfigurationChanged SHOULD call onConfigurationChanged for given renderContext`() = runTest {
        // GIVEN
        val scope = createScope { mockk(relaxed = true) }
        val experienceRenderer = ExperienceRenderer(scope)
        val ownerDirectory = scope.get<StateMachineDirectory>()
        val modalOwner = mockk<StateMachineOwning>(relaxed = true) {
            every { renderContext } returns Modal
        }
        ownerDirectory.setOwner(modalOwner)
        // WHEN
        experienceRenderer.onViewConfigurationChanged(Modal)
        // THEN
        coVerify {
            modalOwner.onConfigurationChanged()
        }
    }

    @Test
    fun `onViewConfigurationChanged SHOULD not call onConfigurationChanged WHEN renderContext is not set`() = runTest {
        // GIVEN
        val scope = createScope { mockk(relaxed = true) }
        val experienceRenderer = ExperienceRenderer(scope)
        val ownerDirectory = scope.get<StateMachineDirectory>()
        val modalOwner = mockk<StateMachineOwning>(relaxed = true) {
            every { renderContext } returns Modal
        }
        ownerDirectory.setOwner(modalOwner)
        // WHEN
        experienceRenderer.onViewConfigurationChanged(Embed("frame1"))
        // THEN
        coVerify(exactly = 0) { modalOwner.onConfigurationChanged() }
    }

    private fun createScope(stateMachine: () -> StateMachine): AppcuesScope {
        return Bootstrap.start(
            modules = arrayListOf(object : AppcuesModule {
                override fun AppcuesScopeDSL.install() {
                    factory { stateMachine() }
                    scoped { AppcuesConfig("abc", "123") }
                    scoped { mockk<AnalyticsTracker>(relaxed = true) }
                    scoped { mockk<ExperienceLifecycleTracker>(relaxed = true) }
                    scoped { StateMachineDirectory() }
                    scoped<CoroutineScope> { AppcuesCoroutineScope(get()) }
                    scoped { mockk<Logcues>(relaxed = true) }
                }
            })
        )
    }
}
