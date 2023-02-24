package com.appcues.data.mapper.styling

import com.appcues.data.remote.appcues.response.styling.StyleColorResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StyleColorMapperTest {

    @Test
    fun `map SHOULD map from StyleColorResponse to ColorComponent`() {
        // Given
        val from = StyleColorResponse(
            light = "#8960FF",
            dark = "#896000"
        )
        // When
        val result = from.mapComponentColor()
        // Then
        assertThat(result).isNotNull()
        with(result) {
            assertThat(light).isEqualTo(0xFF8960FF)
            assertThat(dark).isEqualTo(0xFF896000)
        }
    }

    @Test
    fun `map SHOULD map from StyleColorResponse to ColorComponent WHEN color is only 3 digits`() {
        // Given
        val from = StyleColorResponse(
            light = "#86F",
            dark = "#860"
        )
        // When
        val result = from.mapComponentColor()
        // Then
        with(result) {
            assertThat(light).isEqualTo(0xFF8866FF)
            assertThat(dark).isEqualTo(0xFF886600)
        }
    }

    @Test
    fun `map SHOULD map from StyleColorResponse to ColorComponent WHEN color is only 4 digits`() {
        // Given
        val from = StyleColorResponse(
            light = "#A86F",
            dark = "#B860"
        )
        // When
        val result = from.mapComponentColor()
        // Then
        with(result) {
            assertThat(light).isEqualTo(0xFFAA8866)
            assertThat(dark).isEqualTo(0x00BB8866)
        }
    }

    @Test
    fun `map SHOULD map from StyleColorResponse to ColorComponent WHEN color is 8 digits`() {
        // Given
        val from = StyleColorResponse(
            light = "#A0B1A86F",
            dark = "#FFFFFFFF"
        )
        // When
        val result = from.mapComponentColor()
        // Then
        with(result) {
            assertThat(light).isEqualTo(0x6FA0B1A8)
            assertThat(dark).isEqualTo(0xFFFFFFFF)
        }
    }

    @Test
    fun `map SHOULD map from StyleColorResponse to ColorComponent WHEN dark is null`() {
        // Given
        val from = StyleColorResponse(
            light = "#A86F",
            dark = null
        )
        // When
        val result = from.mapComponentColor()
        // Then
        with(result) {
            assertThat(light).isEqualTo(0xFFAA8866)
            assertThat(dark).isEqualTo(0xFFAA8866)
        }
    }
}
