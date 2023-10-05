package com.appcues.statemachine.effects

import com.appcues.statemachine.Action
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerifySequence
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class AwaitDismissEffectTest {

    @Test
    fun `launch SHOULD return action after complete`() = runTest {
        // GIVEN
        val action = mockk<Action>()
        val awaitDismissEffect = AwaitDismissEffect(action)
        launch { awaitDismissEffect.dismissed() }
        // WHEN
        val result = awaitDismissEffect.launch(mockk())
        // THEN
        assertThat(result).isEqualTo(action)
    }

    @Test
    fun `launch SHOULD call task await`() = runTest {
        // GIVEN
        val action = mockk<Action>()
        val mockTask = mockk<CompletableDeferred<Unit>>(relaxed = true)
        val awaitDismissEffect = AwaitDismissEffect(action).apply {
            task = mockTask
        }
        // WHEN
        awaitDismissEffect.launch(mockk())
        // THEN
        coVerifySequence { mockTask.await() }
    }
}
