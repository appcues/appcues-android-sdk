package com.appcues.data.mapper.step.primitives

import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse.OptionItem
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class OptionSelectPrimitiveMapperTest {
    @Test
    fun `optionSelect mapping SHOULD set minSelection equal to option count WHEN minSelection in data is greater than option count`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(UUID.randomUUID(), null, "label")
        val optionSelectPrimitiveResponse = OptionSelectPrimitiveResponse(
            id = UUID.randomUUID(),
            style = null,
            label = textPrimitiveResponse,
            errorLabel = null,
            selectMode = "multi",
            options = listOf(
                OptionItem("0", TextPrimitiveResponse(UUID.randomUUID(), null, "0")),
                OptionItem("1", TextPrimitiveResponse(UUID.randomUUID(), null, "1")),
                OptionItem("2", TextPrimitiveResponse(UUID.randomUUID(), null, "2")),
            ),
            defaultValue = null,
            minSelections = 7,
            maxSelections = null,
            controlPosition = null,
            displayFormat = null,
            placeholder = null,
            pickerStyle = null,
            selectedColor = null,
            unselectedColor = null,
            accentColor = null,
            attributeName = null,
            leadingFill = null,
        )

        // WHEN
        val optionSelectPrimitive = optionSelectPrimitiveResponse.mapOptionSelectPrimitive()

        // THEN
        assertThat(optionSelectPrimitive.minSelections).isEqualTo(3u)
    }

    @Test
    fun `optionSelect mapping SHOULD set maxSelections equal to minSelections WHEN max is less than min in data`() {
        // GIVEN
        val textPrimitiveResponse = TextPrimitiveResponse(UUID.randomUUID(), null, "label")
        val optionSelectPrimitiveResponse = OptionSelectPrimitiveResponse(
            id = UUID.randomUUID(),
            style = null,
            label = textPrimitiveResponse,
            errorLabel = null,
            selectMode = "multi",
            options = listOf(
                OptionItem("0", TextPrimitiveResponse(UUID.randomUUID(), null, "0")),
                OptionItem("1", TextPrimitiveResponse(UUID.randomUUID(), null, "1")),
                OptionItem("2", TextPrimitiveResponse(UUID.randomUUID(), null, "2")),
            ),
            defaultValue = null,
            minSelections = 2,
            maxSelections = 1,
            controlPosition = null,
            displayFormat = null,
            placeholder = null,
            pickerStyle = null,
            selectedColor = null,
            unselectedColor = null,
            accentColor = null,
            attributeName = null,
            leadingFill = null,
        )

        // WHEN
        val optionSelectPrimitive = optionSelectPrimitiveResponse.mapOptionSelectPrimitive()

        // THEN
        assertThat(optionSelectPrimitive.maxSelections).isEqualTo(2u)
    }
}
