package com.appcues.data.mapper.styling

import com.appcues.data.remote.appcues.response.styling.StyleSizeResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SizeMapperTest {

    @Test
    fun `map SHOULD map from SizeResponse to ComponentSize`() {
        // Given
        val from = StyleSizeResponse(
            width = 1000.0,
            height = 2000.0
        )
        // When
        val result = from.mapComponentSize()
        // Then
        assertThat(result).isNotNull()
        with(result!!) {
            assertThat(width).isEqualTo(1000)
            assertThat(height).isEqualTo(2000)
        }
    }
}
