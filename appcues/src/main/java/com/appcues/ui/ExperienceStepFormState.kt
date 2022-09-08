package com.appcues.ui

import androidx.compose.runtime.mutableStateOf
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.ui.ExperienceStepFormItemState.OptionSelectFormItemState
import com.appcues.ui.ExperienceStepFormItemState.TextInputFormItemState
import java.util.UUID

internal class ExperienceStepFormState {
    private var _formItems: HashMap<UUID, ExperienceStepFormItemState> = hashMapOf()

    private var itemIndex = 0

    val formItems: Collection<ExperienceStepFormItemState>
        get() = _formItems.values.sortedBy { it.index }

    // this can be used by buttons needing to know if they are enabled or not based on required input
    val isFormComplete = mutableStateOf(true)

    fun getValue(primitive: TextInputPrimitive) =
        getItem(primitive).value

    fun setValue(primitive: TextInputPrimitive, value: String) {
        val item = getItem(primitive)
        item.value = value
        updateFormItem(item)
    }

    fun getValue(primitive: OptionSelectPrimitive) =
        getItem(primitive).values

    fun setValue(primitive: OptionSelectPrimitive, values: Set<String>) {
        val item = getItem(primitive)
        item.values = values
        updateFormItem(item)
    }

    private fun getItem(primitive: TextInputPrimitive): TextInputFormItemState {
        var item = _formItems[primitive.id] as? TextInputFormItemState
        if (item != null) return item

        // create new state tracking object
        item = with(primitive) {
            TextInputFormItemState(itemIndex++, id, "textInput", label.text, required, defaultValue ?: "")
        }
        updateFormItem(item)
        return item
    }

    private fun getItem(primitive: OptionSelectPrimitive): OptionSelectFormItemState {
        var item = _formItems[primitive.id] as? OptionSelectFormItemState
        if (item != null) return item

        // create new state tracking object
        item = with(primitive) {
            OptionSelectFormItemState(itemIndex++, id, "optionSelect", label.text, required, defaultValue)
        }
        updateFormItem(item)
        return item
    }

    private fun updateFormItem(item: ExperienceStepFormItemState) {
        _formItems[item.id] = item
        isFormComplete.value = !_formItems.values.any { !it.isComplete }
    }
}

internal sealed class ExperienceStepFormItemState(
    open val index: Int,
    open val id: UUID,
    open val type: String,
    open val label: String,
    open val isRequired: Boolean,
) {

    val isComplete: Boolean
        get() {
            return when (this) {
                is OptionSelectFormItemState -> !isRequired || values.isNotEmpty()
                is TextInputFormItemState -> !isRequired || value.isNotBlank()
            }
        }

    class TextInputFormItemState(
        override val index: Int,
        override val id: UUID,
        override val type: String,
        override val label: String,
        override val isRequired: Boolean,
        var value: String,
    ) : ExperienceStepFormItemState(index, id, type, label, isRequired)

    class OptionSelectFormItemState(
        override val index: Int,
        override val id: UUID,
        override val type: String,
        override val label: String,
        override val isRequired: Boolean,
        var values: Set<String>
    ) : ExperienceStepFormItemState(index, id, type, label, isRequired)
}
