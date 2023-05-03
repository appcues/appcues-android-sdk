package com.appcues.analytics

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date
import java.util.UUID
import kotlin.math.floor

internal class SdkMetricsTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `metrics SHOULD NOT crash`() = runTest {

        val ids = (0..19).map { UUID.randomUUID() }
        val results = mutableListOf<Map<String, Any>>()

        (0..99).asyncAll {
            val index = (floor(it / 5.0)).toInt()
            when (it % 5) {
                0 -> SdkMetrics.tracked(ids[index], Date())
                1 -> SdkMetrics.requested(ids[index].toString())
                2 -> SdkMetrics.responded(ids[index].toString())
                3 -> SdkMetrics.renderStart(ids[index])
                else -> results.add(SdkMetrics.trackRender(ids[index]))
            }
        }

        assertThat(results.count()).isEqualTo(20)
    }

    private suspend fun <T, V> Iterable<T>.asyncAll(coroutine: suspend (T) -> V): Iterable<V> = coroutineScope {
        this@asyncAll.map { async { coroutine(it) } }.awaitAll()
    }
}
