package com.appcues.data.mapper.styling

import com.appcues.data.remote.response.styling.SizeResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SizeMapperTest {

    private val mapper = SizeMapper()

    @Test
    fun `map SHOULD map from SizeResponse to ComponentSize`() {
        // Given
        val from = SizeResponse(
            width = 1000,
            height = 2000
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(width).isEqualTo(1000)
            assertThat(height).isEqualTo(2000)
        }
    }
}
