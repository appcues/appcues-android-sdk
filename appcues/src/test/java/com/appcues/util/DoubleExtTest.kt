package com.appcues.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class DoubleExtTest {

    @Test
    fun `equals pass tests`() {
        assertThat(1.0 eq 1.0).isTrue()
        assertThat(1.1 eq 1.1).isTrue()
        assertThat(1.11 eq 1.11).isTrue()
        assertThat(1.111 eq 1.111).isTrue()
        assertThat(1.1111 eq 1.1111).isTrue()
        assertThat(1.11111 eq 1.11111).isTrue()
        assertThat(1.111112 eq 1.111113).isTrue()
        assertThat(1.11112 eq 1.111113).isFalse()
    }

    @Test
    fun `not equals tests`() {
        assertThat(1.0 ne 2.0).isTrue()
        assertThat(1.1 ne 1.2).isTrue()
        assertThat(1.11 ne 1.12).isTrue()
        assertThat(1.111 ne 1.112).isTrue()
        assertThat(1.11113 ne 1.11114).isTrue()
        assertThat(1.111114 ne 1.111115).isTrue()
        assertThat(1.1111114 ne 1.1111115).isFalse()
    }
}
