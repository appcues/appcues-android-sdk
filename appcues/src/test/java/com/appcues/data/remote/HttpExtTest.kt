package com.appcues.data.remote

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import okhttp3.MediaType
import org.junit.Test
import java.nio.charset.StandardCharsets

internal class HttpExtTest {

    @Test
    fun `charsetOrUTF8 SHOULD return charset UTF-8 WHEN MediaType is null log error properly`() {
        // GIVEN
        val contentType: MediaType? = null
        // WHEN
        val charset = contentType.charsetOrUTF8()
        // THEN
        assertThat(charset).isEqualTo(StandardCharsets.UTF_8)
    }

    @Test
    fun `charsetOrUTF8 SHOULD return charset UTF-8 WHEN MediaType charset is null`() {
        // GIVEN
        val contentType: MediaType = mockk {
            every { charset(any()) } returns null
        }
        // WHEN
        val charset = contentType.charsetOrUTF8()
        // THEN
        assertThat(charset).isEqualTo(StandardCharsets.UTF_8)
    }

    @Test
    fun `charsetOrUTF8 SHOULD return charset UTF-8 WHEN MediaType has charset`() {
        // GIVEN
        val contentType: MediaType = mockk {
            every { charset(any()) } returns StandardCharsets.UTF_16BE
        }
        // WHEN
        val charset = contentType.charsetOrUTF8()
        // THEN
        assertThat(charset).isEqualTo(StandardCharsets.UTF_16BE)
    }
}
