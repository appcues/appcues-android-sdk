package com.appcues.statemachine.effects

import com.appcues.statemachine.Action
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AwaitEffectTest {

    @Test
    fun `launch SHOULD return action after complete`() = runTest {
        // GIVEN
        val action = mockk<Action>()
        val awaitEffect = AwaitEffect(action)
        launch { awaitEffect.complete() }
        // WHEN
        val result = awaitEffect.launch(mockk())
        // THEN
        assertThat(result).isEqualTo(action)
    }

    @Test
    fun `launch SHOULD call task await`() = runTest {
        // GIVEN
        val action = mockk<Action>()
        val task = mockk<CompletableDeferred<Unit>>(relaxed = true)
        val awaitEffect = AwaitEffect(action, task)
        // WHEN
        awaitEffect.launch(mockk())
        // THEN
        coVerifySequence { task.await() }
    }
}
