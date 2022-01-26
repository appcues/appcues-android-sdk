package com.appcues.data.mapper.styling

import com.appcues.data.remote.response.styling.StyleResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StyleMapperTest {

    private val mapper = StyleMapper()

    @Test
    fun `map SHOULD map from StyleResponse to StyleComponent`() {
        // Given
        val from = StyleResponse(
            marginLeading = 1,
            marginTop = 2,
            marginTrailing = 3,
            marginBottom = 4,
            paddingLeading = 5,
            paddingTop = 6,
            paddingBottom = 7,
            paddingTrailing = 8,
            cornerRadius = 9,
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(marginLeading).isEqualTo(1)
            assertThat(marginTop).isEqualTo(2)
            assertThat(marginTrailing).isEqualTo(3)
            assertThat(marginBottom).isEqualTo(4)
            assertThat(paddingLeading).isEqualTo(5)
            assertThat(paddingTop).isEqualTo(6)
            assertThat(paddingBottom).isEqualTo(7)
            assertThat(paddingTrailing).isEqualTo(8)
            assertThat(cornerRadius).isEqualTo(9)
        }
    }

    @Test
    fun `map SHOULD map from StyleResponse to StyleComponent WITH default values WHEN StyleResponse is null`() {
        // Given
        val from = null
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(marginLeading).isEqualTo(0)
            assertThat(marginTop).isEqualTo(0)
            assertThat(marginTrailing).isEqualTo(0)
            assertThat(marginBottom).isEqualTo(0)
            assertThat(paddingLeading).isEqualTo(0)
            assertThat(paddingTop).isEqualTo(0)
            assertThat(paddingBottom).isEqualTo(0)
            assertThat(paddingTrailing).isEqualTo(0)
            assertThat(cornerRadius).isEqualTo(0)
        }
    }
}
