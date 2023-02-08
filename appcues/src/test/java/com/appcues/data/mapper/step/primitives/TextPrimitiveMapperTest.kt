package com.appcues.data.mapper.step.primitives

import com.appcues.data.mapper.AppcuesMappingException
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextSpanResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class TextPrimitiveMapperTest {

    @Test
    fun `text primitive mapping SHOULD map all spans properly and also concatenate to the text label`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(
            UUID.randomUUID(), null, null,
            spans = arrayListOf(
                TextSpanResponse("1"), TextSpanResponse("2"), TextSpanResponse("3")
            )
        )

        // WHEN
        val textPrimitive = textPrimitiveResponse.mapTextPrimitive()

        // THEN
        assertThat(textPrimitive.spans).hasSize(3)
        assertThat(textPrimitive.spans[0].text).isEqualTo("1")
        assertThat(textPrimitive.spans[1].text).isEqualTo("2")
        assertThat(textPrimitive.spans[2].text).isEqualTo("3")
        assertThat(textPrimitive.textDescription).isEqualTo("123")
    }

    @Test
    fun `text primitive mapping SHOULD be compatible with older versions of the textResponse`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(
            UUID.randomUUID(), null, "old text",
        )

        // WHEN
        val textPrimitive = textPrimitiveResponse.mapTextPrimitive()

        // THEN
        assertThat(textPrimitive.spans).hasSize(1)
        assertThat(textPrimitive.spans[0].text).isEqualTo("old text")
        assertThat(textPrimitive.textDescription).isEqualTo("old text")
    }

    @Test(expected = AppcuesMappingException::class)
    fun `text primitive mapping SHOULD throw error if no text or spans are provided`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(UUID.randomUUID(), null, null)
        // WHEN
        textPrimitiveResponse.mapTextPrimitive()
        // Then text will capture exception
    }

    @Test
    fun `text primitive mapping SHOULD be valid when text is empty instead of null`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(UUID.randomUUID(), null, "")

        // WHEN
        val textPrimitive = textPrimitiveResponse.mapTextPrimitive()

        // THEN
        assertThat(textPrimitive.spans).hasSize(1)
        assertThat(textPrimitive.spans[0].text).isEmpty()
        assertThat(textPrimitive.textDescription).isEmpty()
    }

    @Test
    fun `text primitive mapping SHOULD be valid when spans is an empty list instead of null`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(UUID.randomUUID(), null, null, spans = arrayListOf())

        // WHEN
        val textPrimitive = textPrimitiveResponse.mapTextPrimitive()

        // THEN
        assertThat(textPrimitive.spans).hasSize(0)
        assertThat(textPrimitive.textDescription).isEmpty()
    }
}
