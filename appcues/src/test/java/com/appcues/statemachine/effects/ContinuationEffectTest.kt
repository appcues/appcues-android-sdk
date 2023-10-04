package com.appcues.statemachine.effects

import com.appcues.statemachine.Action.StartStep
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class ContinuationEffectTest {

    @Test
    fun `launch SHOULD return action`() = runTest {
        // GIVEN
        val action = StartStep(mockk())
        val continuationEffect = ContinuationEffect(action)
        // WHEN
        val result = continuationEffect.launch(mockk())
        // THEN
        assertThat(result).isEqualTo(action)
    }
}
