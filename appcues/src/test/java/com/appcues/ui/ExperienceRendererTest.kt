package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleTracker
import com.appcues.analytics.track
import com.appcues.data.model.Experience
import com.appcues.data.model.Experiment
import com.appcues.data.model.RenderContext
import com.appcues.mocks.mockExperience
import com.appcues.mocks.mockExperienceExperiment
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import com.appcues.ui.ExperienceRenderer.RenderingResult
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module
import org.koin.mp.KoinPlatformTools
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class ExperienceRendererTest {

    @After
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is on last step`() = runTest {
        // GIVEN
        val state = RenderingStep(mockExperience(), 3, false)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { this@mockk.handleAction(any()) } returns Success(Idling)
        }
        val scope = initScope(stateMachine)
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
        val state = RenderingStep(mockExperience(), 2, false)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
            coEvery { this@mockk.handleAction(any()) } returns Success(Idling)
        }
        val scope = initScope(stateMachine)
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
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
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
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
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
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
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
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.show(experience)

        // THEN
        verify { analyticsTracker.track(experiment) }
    }

    private fun initScope(stateMachine: StateMachine): Scope {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()
        val scopeId = UUID.randomUUID().toString()
        return startKoin {
            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
                        scoped { scope }
                        factory { stateMachine }
                        scoped { AppcuesConfig("abc", "123") }
                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
                        scoped { mockk<ExperienceLifecycleTracker>(relaxed = true) }
                        scoped { StateMachineDirectory() }
                    }
                }
            )
        }.koin.getScope(scopeId)
    }
}
