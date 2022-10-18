package com.appcues.ui

import com.appcues.AppcuesConfig
import com.appcues.analytics.AnalyticsEvent
import com.appcues.analytics.AnalyticsTracker
import com.appcues.data.model.Experiment
import com.appcues.mocks.mockExperience
import com.appcues.mocks.mockExperienceExperiment
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
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
class ExperienceRendererTest {
    @After
    fun shutdown() {
        stopKoin()
    }

    @Test
    fun `dismissCurrentExperience SHOULD mark complete WHEN current state is on last step`() = runTest {
        // GIVEN
        val state = RenderingStep(mockExperience(), 3, false)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
        }
        val scope = initScope(stateMachine)
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = true, destroyed = false)) }
    }

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is not on last step`() = runTest {
        // GIVEN
        val state = RenderingStep(mockExperience(), 2, false)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { state }
        }
        val scope = initScope(stateMachine)
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    @Test
    fun `show SHOULD NOT show experience WHEN an experiment is active AND group is control`() = runTest {
        // GIVEN
        val experiment = Experiment("experiment1", "control")
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        val showResult = experienceRenderer.show(experience)

        // THEN
        assertThat(showResult).isFalse()
        coVerify { stateMachine.handleAction(StartExperience(experience)) wasNot Called }
    }

    @Test
    fun `show SHOULD show experience WHEN an experiment is active AND group is exposed`() = runTest {
        // GIVEN
        val experiment = Experiment("experiment1", "exposed")
        val experience = mockExperienceExperiment(experiment)
        val stateMachine = mockk<StateMachine>(relaxed = true) {
            every { this@mockk.state } answers { Idling }
            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
        }
        val scope = initScope(stateMachine)
        val analyticsTracker: AnalyticsTracker = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        val showResult = experienceRenderer.show(experience)

        // THEN
        assertThat(showResult).isTrue()
        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    }

    @Test
    fun `show SHOULD track experiment_entered with group=control WHEN an experiment is active AND group is control`() = runTest {
        // GIVEN
        val experiment = Experiment("experiment1", "control")
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
        verify {
            analyticsTracker.track(AnalyticsEvent.ExperimentEntered, mapOf("experimentId" to "experiment1", "group" to "control"), false)
        }
    }

    @Test
    fun `show SHOULD track experiment_entered with group=exposed WHEN an experiment is active AND group is exposed`() = runTest {
        // GIVEN
        val experiment = Experiment("experiment1", "exposed")
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
        verify {
            analyticsTracker.track(AnalyticsEvent.ExperimentEntered, mapOf("experimentId" to "experiment1", "group" to "exposed"), false)
        }
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
                        scoped { stateMachine }
                        scoped { AppcuesConfig("abc", "123") }
                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
                    }
                }
            )
        }.koin.getScope(scopeId)
    }
}
