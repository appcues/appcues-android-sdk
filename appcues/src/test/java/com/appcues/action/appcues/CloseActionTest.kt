package com.appcues.action.appcues

import com.appcues.AppcuesKoinTestRule
import com.appcues.AppcuesScopeTest
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

@OptIn(ExperimentalCoroutinesApi::class)
internal class CloseActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = AppcuesKoinTestRule()

    @Test
    fun `close SHOULD have expected type name`() {
        assertThat(CloseAction.TYPE).isEqualTo("@appcues/close")
    }

    @Test
    fun `close SHOULD trigger StateMachine EndExperience action`() = runTest {
        // GIVEN
        val stateMachine: StateMachine = get()
        val action = CloseAction(mapOf(), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(destroyed = false, markComplete = false)) }
    }

    @Test
    fun `close SHOULD trigger StateMachine EndExperience action with markComplete true WHEN config is true`() = runTest {
        // GIVEN
        val stateMachine: StateMachine = get()
        val action = CloseAction(mapOf("markComplete" to true), stateMachine)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { stateMachine.handleAction(EndExperience(destroyed = false, markComplete = true)) }
    }
}
