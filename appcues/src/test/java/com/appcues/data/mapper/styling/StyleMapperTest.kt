package com.appcues.data.mapper.styling

import com.appcues.data.remote.appcues.response.styling.StyleResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StyleMapperTest {

    @Test
    fun `map SHOULD map from StyleResponse to StyleComponent`() {
        // Given
        val from = StyleResponse(
            marginLeading = 1.0,
            marginTop = 2.0,
            marginTrailing = 3.0,
            marginBottom = 4.0,
            paddingLeading = 5.0,
            paddingTop = 6.0,
            paddingBottom = 7.0,
            paddingTrailing = 8.0,
            cornerRadius = 9.0,
        )
        // When
        val result = from.mapComponentStyle()
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
        val result = from.mapComponentStyle()
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
