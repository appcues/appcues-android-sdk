package com.appcues.data.mapper.step.primitives

import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.OptionSelectPrimitiveResponse.OptionItem
import com.appcues.data.remote.response.step.primitive.PrimitiveResponse.TextPrimitiveResponse
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
        )

        // WHEN
        val optionSelectPrimitive = optionSelectPrimitiveResponse.mapOptionSelectPrimitive()

        // THEN
        assertThat(optionSelectPrimitive.maxSelections).isEqualTo(2u)
    }
}

//    @Test
//    fun `optionSelect SHOULD be complete WHEN selection mode is multi AND minSelections greater than option count AND all selected`() {
//        // GIVEN
//        val options = optionItems(3)
//        val optionSelect = OptionSelectPrimitive(
//            id = UUID.randomUUID(),
//            label = label,
//            minSelections = 7u, // invalid, 3 is the max it could be for 3 options
//            selectMode = MULTIPLE,
//            options = options,
//        )
//        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)
//
//        // WHEN
//        optionSelectFormItemState.setValue(options[0].value)
//        optionSelectFormItemState.setValue(options[1].value)
//        optionSelectFormItemState.setValue(options[2].value)
//
//        // THEN
//        assertThat(optionSelectFormItemState.isComplete).isTrue()
//    }

// fun `optionSelect SHOULD be complete WHEN selection mode is multi AND maxSelections less than minSelections and the min is selected`() {
