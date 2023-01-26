package com.appcues.data.model

import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive.OptionItem
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperienceStepFormItemState.OptionSelectFormItemState
import com.appcues.data.model.ExperienceStepFormItemState.TextInputFormItemState
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.data.model.styling.ComponentSelectMode.SINGLE
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.UUID

class ExperienceStepFormStateTest {

    private val label = TextPrimitive(id = UUID.randomUUID(), text = "label", spans = listOf())

    @Test
    fun `form state SHOULD be valid by default`() {
        val formState = ExperienceStepFormState()
        assertThat(formState.isFormComplete).isTrue()
    }

    @Test
    fun `textInput SHOULD NOT be complete WHEN required is true AND no input`() {
        // GIVEN
        val textInput = TextInputPrimitive(id = UUID.randomUUID(), label = label, required = true)
        val textInputFormItemState = TextInputFormItemState(0, textInput)

        // THEN
        assertThat(textInputFormItemState.isComplete).isFalse()
    }

    @Test
    fun `textInput SHOULD be complete WHEN required is true AND has input`() {
        // GIVEN
        val textInput = TextInputPrimitive(id = UUID.randomUUID(), label = label, required = true)
        val textInputFormItemState = TextInputFormItemState(0, textInput)

        // WHEN
        textInputFormItemState.setValue("text")

        // THEN
        assertThat(textInputFormItemState.isComplete).isTrue()
    }

    @Test
    fun `textInput SHOULD NOT be complete WHEN required is true AND has empty string input`() {
        // GIVEN
        val textInput = TextInputPrimitive(id = UUID.randomUUID(), label = label, required = true)
        val textInputFormItemState = TextInputFormItemState(0, textInput)

        // WHEN
        textInputFormItemState.setValue("")

        // THEN
        assertThat(textInputFormItemState.isComplete).isFalse()
    }

    @Test
    fun `optionSelect SHOULD NOT be complete WHEN selectMode is single AND minSelections is 1 AND no selection`() {
        // GIVEN
        val options = optionItems(3)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            selectMode = SINGLE,
            minSelections = 1u,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isFalse()
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selectMode is single AND minSelections is unset AND no selection`() {
        // GIVEN
        val options = optionItems(3)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            selectMode = SINGLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selectMode is single AND minSelections is 1 AND has selection`() {
        // GIVEN
        val options = optionItems(3)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            selectMode = SINGLE,
            minSelections = 1u,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[0].value)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `optionSelect SHOULD have single value WHEN selectMode is single`() {
        // GIVEN
        val options = optionItems(3)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            selectMode = SINGLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[0].value)
        optionSelectFormItemState.setValue(options[1].value)
        optionSelectFormItemState.setValue(options[2].value)

        // THEN
        assertThat(optionSelectFormItemState.values.value.count()).isEqualTo(1)
        assertThat(optionSelectFormItemState.values.value.first()).isEqualTo(options[2].value)
    }

    @Test
    fun `optionSelect values SHOULD match the selection order WHEN multiple selections made`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value) // "4"
        optionSelectFormItemState.setValue(options[1].value) // "4,1"
        optionSelectFormItemState.setValue(options[3].value) // "4,1,3"
        optionSelectFormItemState.setValue(options[1].value) // "4,3"
        optionSelectFormItemState.setValue(options[0].value) // "4,3,0"
        optionSelectFormItemState.setValue(options[4].value) // "3,0"
        optionSelectFormItemState.setValue(options[2].value) // "3,0,2"

        // THEN
        assertThat(optionSelectFormItemState.value).isEqualTo("3\n0\n2")
    }

    @Test
    fun `optionSelect SHOULD NOT be complete WHEN selection mode is multi AND selections less than minSelections`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            minSelections = 3u,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value)
        optionSelectFormItemState.setValue(options[1].value)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isFalse()
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selection mode is multi AND selections greater than or equal to minSelections`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            minSelections = 3u,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value)
        optionSelectFormItemState.setValue(options[1].value)
        optionSelectFormItemState.setValue(options[0].value)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `optionSelect SHOULD NOT allow more than max selections WHEN selection mode is multi`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            maxSelections = 3u,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value)
        optionSelectFormItemState.setValue(options[1].value)
        optionSelectFormItemState.setValue(options[0].value)
        optionSelectFormItemState.setValue(options[2].value) // ignored

        // THEN
        assertThat(optionSelectFormItemState.values.value.count()).isEqualTo(3)
        assertThat(optionSelectFormItemState.value).isEqualTo("4\n1\n0")
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selection mode is multi AND selections less than or equal to maxSelections`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            maxSelections = 3u,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value)
        optionSelectFormItemState.setValue(options[1].value)
        optionSelectFormItemState.setValue(options[0].value)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selection mode is multi AND selections between min and max selections`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            minSelections = 2u,
            maxSelections = 3u,
            selectMode = MULTIPLE,
            options = options
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        optionSelectFormItemState.setValue(options[4].value)
        optionSelectFormItemState.setValue(options[1].value)

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `optionSelect SHOULD NOT be complete WHEN selection mode is multi AND default value is greater than maxSelections`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            maxSelections = 3u,
            selectMode = MULTIPLE,
            options = options,
            defaultValue = setOf("0", "1", "2", "3")
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        // NOTE: this can only happen right now inside of ExperienceStepFormState where a new form item is registered and an initial
        // value is sent back to the UI - the default value. So we're simulating that process here. Calls to `setValue(x)` will not
        // allow the optionSelect to have more than the valid max values, it will ignore new items above that value
        optionSelectFormItemState.values.value = optionSelect.defaultValue

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isFalse()
    }

    @Test
    fun `optionSelect SHOULD be complete WHEN selection mode is multi AND default value is greater than max AND items unselected`() {
        // GIVEN
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            maxSelections = 3u,
            selectMode = MULTIPLE,
            options = options,
            defaultValue = setOf("0", "1", "2", "3")
        )
        val optionSelectFormItemState = OptionSelectFormItemState(0, optionSelect)

        // WHEN
        // NOTE: this can only happen right now inside of ExperienceStepFormState where a new form item is registered and an initial
        // value is sent back to the UI - the default value. So we're simulating that process here. Calls to `setValue(x)` will not
        // allow the optionSelect to have more than the valid max values, it will ignore new items above that value
        optionSelectFormItemState.values.value = optionSelect.defaultValue
        optionSelectFormItemState.setValue(options[0].value) // deselect an item to get back to valid range

        // THEN
        assertThat(optionSelectFormItemState.isComplete).isTrue()
    }

    @Test
    fun `form state SHOULD be complete WHEN elements within are complete`() {
        // GIVEN
        val formState = ExperienceStepFormState()
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            minSelections = 1u,
            selectMode = MULTIPLE,
            options = options,
        )
        val textInput = TextInputPrimitive(id = UUID.randomUUID(), label = label, required = true)
        formState.register(textInput)
        formState.register(optionSelect)
        formState.setValue(optionSelect, options[0].value)
        formState.setValue(textInput, "text")

        // THEN
        assertThat(formState.isFormComplete).isTrue()
    }

    @Test
    fun `form state SHOULD NOT be complete WHEN elements within are not complete`() {
        // GIVEN
        val formState = ExperienceStepFormState()
        val options = optionItems(5)
        val optionSelect = OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = label,
            minSelections = 3u,
            selectMode = MULTIPLE,
            options = options,
        )
        val textInput = TextInputPrimitive(id = UUID.randomUUID(), label = label, required = true)
        formState.register(textInput)
        formState.register(optionSelect)
        formState.setValue(optionSelect, options[0].value)
        formState.setValue(textInput, "text")

        // THEN
        assertThat(formState.isFormComplete).isFalse()
    }

    private fun optionItems(count: Int) = (0..count).map {
        OptionItem("$it", TextPrimitive(UUID.randomUUID(), text = "$it", spans = listOf()))
    }
}
