package com.appcues.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
internal class ExperienceRendererTest {

//    @After
    //    fun shutdown() {
    //        stopKoin()
    //    }
    //
    //    @Test
    //    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is on last step`() = runTest {
    //        // GIVEN
    //        val state = RenderingStep(mockExperience(), 3, false)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { state }
    //        }
    //        val scope = initScope(stateMachine)
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        experienceRenderer.dismiss(markComplete = false, destroyed = false)
    //
    //        // THEN
    //        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    //    }
    //
    //    @Test
    //    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is not on last step`() = runTest {
    //        // GIVEN
    //        val state = RenderingStep(mockExperience(), 2, false)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { state }
    //        }
    //        val scope = initScope(stateMachine)
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        experienceRenderer.dismiss(markComplete = false, destroyed = false)
    //
    //        // THEN
    //        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    //    }
    //
    //    @Test
    //    fun `show SHOULD NOT show experience WHEN an experiment is active AND group is control`() = runTest {
    //        // GIVEN
    //        val experiment = Experiment(
    //            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
    //            group = "control",
    //            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
    //            goalId = "my-goal",
    //            contentType = "my-content-type"
    //        )
    //        val experience = mockExperienceExperiment(experiment)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { Idling }
    //            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
    //        }
    //        val scope = initScope(stateMachine)
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        val showResult = experienceRenderer.show(experience)
    //
    //        // THEN
    //        assertThat(showResult).isFalse()
    //        coVerify { stateMachine.handleAction(StartExperience(experience)) wasNot Called }
    //    }
    //
    //    @Test
    //    fun `show SHOULD show experience WHEN an experiment is active AND group is exposed`() = runTest {
    //        // GIVEN
    //        val experiment = Experiment(
    //            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
    //            group = "exposed",
    //            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
    //            goalId = "my-goal",
    //            contentType = "my-content-type"
    //        )
    //        val experience = mockExperienceExperiment(experiment)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { Idling }
    //            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
    //        }
    //        val scope = initScope(stateMachine)
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        val showResult = experienceRenderer.show(experience)
    //
    //        // THEN
    //        assertThat(showResult).isTrue()
    //        coVerify { stateMachine.handleAction(StartExperience(experience)) }
    //    }
    //
    //    @Test
    //    fun `show SHOULD track experiment_entered with group=control WHEN an experiment is active AND group is control`() = runTest {
    //        // GIVEN
    //        val experiment = Experiment(
    //            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
    //            group = "control",
    //            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
    //            goalId = "my-goal",
    //            contentType = "my-content-type"
    //        )
    //        val experience = mockExperienceExperiment(experiment)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { Idling }
    //            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
    //        }
    //        val scope = initScope(stateMachine)
    //        val analyticsTracker: AnalyticsTracker = scope.get()
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        experienceRenderer.show(experience)
    //
    //        // THEN
    //        verify {
    //            analyticsTracker.track(
    //                AnalyticsEvent.ExperimentEntered,
    //                mapOf(
    //                    "experimentId" to "06f9bf87-1921-4919-be55-429b278bf578",
    //                    "experimentGroup" to "control",
    //                    "experimentExperienceId" to "d84c9d01-aa27-4cbb-b832-ee03720e04fc",
    //                    "experimentGoalId" to "my-goal",
    //                    "experimentContentType" to "my-content-type",
    //                ),
    //                false
    //            )
    //        }
    //    }
    //
    //    @Test
    //    fun `show SHOULD track experiment_entered with group=exposed WHEN an experiment is active AND group is exposed`() = runTest {
    //        // GIVEN
    //        val experiment = Experiment(
    //            id = UUID.fromString("06f9bf87-1921-4919-be55-429b278bf578"),
    //            group = "exposed",
    //            experienceId = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
    //            goalId = "my-goal",
    //            contentType = "my-content-type"
    //        )
    //        val experience = mockExperienceExperiment(experiment)
    //        val stateMachine = mockk<StateMachine>(relaxed = true) {
    //            every { this@mockk.state } answers { Idling }
    //            coEvery { this@mockk.handleAction(any()) } answers { Success(Idling) }
    //        }
    //        val scope = initScope(stateMachine)
    //        val analyticsTracker: AnalyticsTracker = scope.get()
    //        val experienceRenderer = ExperienceRenderer(scope, scope.get(), scope.get())
    //
    //        // WHEN
    //        experienceRenderer.show(experience)
    //
    //        // THEN
    //        verify {
    //            analyticsTracker.track(
    //                AnalyticsEvent.ExperimentEntered,
    //                mapOf(
    //                    "experimentId" to "06f9bf87-1921-4919-be55-429b278bf578",
    //                    "experimentGroup" to "exposed",
    //                    "experimentExperienceId" to "d84c9d01-aa27-4cbb-b832-ee03720e04fc",
    //                    "experimentGoalId" to "my-goal",
    //                    "experimentContentType" to "my-content-type",
    //                ),
    //                false
    //            )
    //        }
    //    }
    //
    //    private fun initScope(stateMachine: StateMachine): Scope {
    //        // close any existing instance
    //        KoinPlatformTools.defaultContext().getOrNull()?.close()
    //        val scopeId = UUID.randomUUID().toString()
    //        return startKoin {
    //            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
    //            modules(
    //                module {
    //                    scope(named(scopeId)) {
    //                        scoped { stateMachine }
    //                        scoped { AppcuesConfig("abc", "123") }
    //                        scoped { mockk<AnalyticsTracker>(relaxed = true) }
    //                        scoped { mockk<ExperienceLifecycleTracker>(relaxed = true) }
    //                        scoped { mockk<AppcuesCoroutineScope>(relaxed = true) }
    //                    }
    //                }
    //            )
    //        }.koin.getScope(scopeId)
    //    }
}
