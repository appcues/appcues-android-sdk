package com.appcues.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class FloatExtTest {

    @Test
    fun `equals pass tests`() {
        assertThat(1f eq 1f).isTrue()
        assertThat(1.1f eq 1.1f).isTrue()
        assertThat(1.11f eq 1.11f).isTrue()
        assertThat(1.111f eq 1.111f).isTrue()
        assertThat(1.1113f eq 1.1114f).isFalse()
        assertThat(1.11114f eq 1.11115f).isTrue()
    }

    @Test
    fun `not equals tests`() {
        assertThat(1f ne 2f).isTrue()
        assertThat(1.1f ne 1.2f).isTrue()
        assertThat(1.11f ne 1.12f).isTrue()
        assertThat(1.111f ne 1.112f).isTrue()
        assertThat(1.1113f ne 1.1113f).isFalse()
        assertThat(1.11114f ne 1.11125f).isTrue()
    }
}
