package com.appcues.ui.utils

import androidx.compose.runtime.mutableStateOf
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.data.model.styling.ComponentSelectMode.SINGLE
import com.appcues.ui.utils.ExperienceStepFormItemState.OptionSelectFormItemState
import com.appcues.ui.utils.ExperienceStepFormItemState.TextInputFormItemState
import java.util.UUID

internal class ExperienceStepFormState {
    private var _formItems: HashMap<UUID, ExperienceStepFormItemState> = hashMapOf()

    private var itemIndex = 0

    val formItems: Collection<ExperienceStepFormItemState>
        get() = _formItems.values.sortedBy { it.index }

    // this can be used by buttons needing to know if they are enabled or not based on required input
    val isFormComplete = mutableStateOf(true)

    fun getValue(primitive: TextInputPrimitive) =
        getItem(primitive).text

    fun setValue(primitive: TextInputPrimitive, value: String) {
        val item = getItem(primitive)
        item.setValue(value)
        updateFormItem(item)
    }

    fun getValue(primitive: OptionSelectPrimitive) =
        getItem(primitive).values

    fun setValue(primitive: OptionSelectPrimitive, value: String) {
        val item = getItem(primitive)
        item.setValue(value)
        updateFormItem(item)
    }

    private fun getItem(primitive: TextInputPrimitive): TextInputFormItemState {
        var item = _formItems[primitive.id] as? TextInputFormItemState
        if (item != null) return item

        // create new state tracking object
        item = with(primitive) {
            TextInputFormItemState(itemIndex++, this).apply {
                text.value = defaultValue ?: ""
            }
        }
        updateFormItem(item)
        return item
    }

    private fun getItem(primitive: OptionSelectPrimitive): OptionSelectFormItemState {
        var item = _formItems[primitive.id] as? OptionSelectFormItemState
        if (item != null) return item

        // create new state tracking object
        item = with(primitive) {
            OptionSelectFormItemState(itemIndex++, this).apply {
                // it is possible this sets a default value with a number of items
                // greater than the max allowed, setting in invalid state.  This is allowed,
                // but the user would need to unselect some to be able to make changes or
                // get back to a valid state to continue.
                values.value = defaultValue
            }
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
                is TextInputFormItemState -> !isRequired || text.value.isNotBlank()
                is OptionSelectFormItemState -> {
                    when (primitive.selectMode) {
                        SINGLE -> !isRequired || values.value.isNotEmpty()
                        MULTIPLE -> {
                            if (values.value.count().toUInt() < primitive.minSelections) return false
                            if (primitive.maxSelections != null && values.value.count().toUInt() > primitive.maxSelections) return false
                            return true
                        }
                    }
                }
            }
        }

    val value: String
        get() =
            when (this) {
                is TextInputFormItemState -> text.value
                is OptionSelectFormItemState -> values.value.joinToString(",") // need actual CSV-ifying
            }

    class TextInputFormItemState(
        override val index: Int,
        val primitive: TextInputPrimitive,
    ) : ExperienceStepFormItemState(index, primitive.id, "textInput", primitive.label.text, primitive.required) {
        var text = mutableStateOf("")

        fun setValue(newValue: String) {
            text.value = newValue
        }
    }

    class OptionSelectFormItemState(
        override val index: Int,
        val primitive: OptionSelectPrimitive,
    ) : ExperienceStepFormItemState(
        index = index,
        id = primitive.id,
        type = "optionSelect",
        label = primitive.label.text,
        isRequired = primitive.minSelections > 0u
    ) {
        var values = mutableStateOf(setOf<String>())

        fun setValue(newValue: String) {
            when (primitive.selectMode) {
                SINGLE -> values.value = setOf(newValue)
                MULTIPLE -> {
                    if (values.value.contains(newValue)) {
                        values.value = values.value.filter { it != newValue }.toSet()
                    } else {
                        if (primitive.maxSelections != null) {
                            if (values.value.count().toUInt() < primitive.maxSelections) {
                                val updated = values.value.toMutableSet()
                                updated.add(newValue)
                                values.value = updated
                            } else {
                                // Would be selecting more than the max, so no change
                            }
                        } else {
                            val updated = values.value.toMutableSet()
                            updated.add(newValue)
                            values.value = updated
                        }
                    }
                }
            }
        }
    }
}
