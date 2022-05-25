package com.appcues.action.appcues

import com.appcues.AppcuesKoinTestRule
import com.appcues.AppcuesScopeTest
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.StateMachine
import com.appcues.statemachine.StepReference.StepId
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.statemachine.StepReference.StepOffset
import com.google.common.truth.Truth
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContinueActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = AppcuesKoinTestRule()

    @Test
    fun `continue SHOULD have expected type name`() {
        Truth.assertThat(ContinueAction.TYPE).isEqualTo("@appcues/continue")
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with index WHEN config has index`() = runTest {
        // GIVEN
        val stateMachine: StateMachine = get()
        val action = ContinueAction(mapOf("index" to 1), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(StartStep(StepIndex(1))) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset WHEN config has offset`() = runTest {
        // GIVEN
        val stateMachine: StateMachine = get()
        val action = ContinueAction(mapOf("offset" to -1), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(StartStep(StepOffset(-1))) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with step id WHEN config has step id`() = runTest {
        // GIVEN
        val stepId = UUID.randomUUID()
        val stateMachine: StateMachine = get()
        val action = ContinueAction(mapOf("stepID" to stepId.toString()), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(StartStep(StepId(stepId))) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset 1 by default`() = runTest {
        // GIVEN
        val stateMachine: StateMachine = get()
        val action = ContinueAction(mapOf(), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(StartStep(StepOffset(1))) }
    }
}
