package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.appcues.statemachine.StepReference.StepId
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.ui.ExperienceRenderer
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
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `continue SHOULD have expected type name`() {
        Truth.assertThat(ContinueAction.TYPE).isEqualTo("@appcues/continue")
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with index WHEN config has index`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = ContinueAction(mapOf("index" to 1), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(StepIndex(1)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset WHEN config has offset`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = ContinueAction(mapOf("offset" to -1), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(StepOffset(-1)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with step id WHEN config has step id`() = runTest {
        // GIVEN
        val stepId = UUID.randomUUID()
        val experienceRenderer: ExperienceRenderer = get()
        val action = ContinueAction(mapOf("stepID" to stepId.toString()), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(StepId(stepId)) }
    }

    @Test
    fun `continue SHOULD trigger StateMachine StartStep with offset 1 by default`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = ContinueAction(mapOf(), experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.show(StepOffset(1)) }
    }
}
