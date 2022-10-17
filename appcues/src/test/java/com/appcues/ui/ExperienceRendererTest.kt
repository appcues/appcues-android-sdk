package com.appcues.ui

import com.appcues.mocks.mockExperience
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.State
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StateMachine
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
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
        val scope = initScope(RenderingStep(mockExperience(), 3, false))
        val stateMachine: StateMachine = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = true, destroyed = false)) }
    }

    @Test
    fun `dismissCurrentExperience SHOULD NOT mark complete WHEN current state is not on last step`() = runTest {
        // GIVEN
        val scope = initScope(RenderingStep(mockExperience(), 2, false))
        val stateMachine: StateMachine = scope.get()
        val experienceRenderer = ExperienceRenderer(scope)

        // WHEN
        experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false)

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(markComplete = false, destroyed = false)) }
    }

    private fun initScope(state: State): Scope {
        // close any existing instance
        KoinPlatformTools.defaultContext().getOrNull()?.close()
        val scopeId = UUID.randomUUID().toString()
        return startKoin {
            val scope = koin.getOrCreateScope(scopeId = scopeId, qualifier = named(scopeId))
            modules(
                module {
                    scope(named(scopeId)) {
                        scoped {
                            mockk<StateMachine>(relaxed = true) {
                                every { this@mockk.state } answers { state }
                            }
                        }
                    }
                }
            )
        }.koin.getScope(scopeId)
    }
}
