package com.appcues.data.mapper.styling

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class NormalizeToArgbLongTest {

    @Test
    fun `normalizeToArgbLong SHOULD properly convert following values`() {
        assertThat(normalizeToArgbLong("#876")).isEqualTo(0xFF887766)
        assertThat(normalizeToArgbLong("#8765")).isEqualTo(0x55887766)
        assertThat(normalizeToArgbLong("#87654321")).isEqualTo(0x21876543)
        assertThat(normalizeToArgbLong("#876543")).isEqualTo(0xFF876543)
    }
}
