package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class ExperienceActionEffectTest {

    @Test
    fun `launch SHOULD return action`() = runTest {
        // GIVEN
        val actions = listOf<ExperienceAction>(mockk(), mockk(), mockk())
        val effect = ExperienceActionEffect(actions)
        val actionProcessor = mockk<ActionProcessor>(relaxed = true)
        // WHEN
        val result = effect.launch(actionProcessor)
        // THEN
        assertThat(result).isNull()
        coVerify { actionProcessor.processPostFlowActions(actions) }
    }
}
