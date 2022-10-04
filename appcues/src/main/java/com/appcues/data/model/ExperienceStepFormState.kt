package com.appcues.data.model

import androidx.compose.runtime.mutableStateOf
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperienceStepFormItemState.OptionSelectFormItemState
import com.appcues.data.model.ExperienceStepFormItemState.TextInputFormItemState
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.data.model.styling.ComponentSelectMode.SINGLE
import java.util.UUID

internal class ExperienceStepFormState {
    private var _formItems: HashMap<UUID, ExperienceStepFormItemState> = hashMapOf()

    private var itemIndex = 0

    val formItems: Collection<ExperienceStepFormItemState>
        get() = _formItems.values.sortedBy { it.index }

    // this can be used by buttons needing to know if they are enabled or not based on required input
    val isFormComplete = mutableStateOf(true)

    fun register(primitive: ExperiencePrimitive) {
        if (_formItems.containsKey(primitive.id)) return // already registered - ok

        when (primitive) {
            is TextInputPrimitive -> TextInputFormItemState(itemIndex++, primitive).apply {
                text.value = primitive.defaultValue ?: ""
            }
            is OptionSelectPrimitive -> OptionSelectFormItemState(itemIndex++, primitive).apply {
                // it is possible this sets a default value with a number of items
                // greater than the max allowed, setting in invalid state.  This is allowed,
                // but the user would need to unselect some to be able to make changes or
                // get back to a valid state to continue.
                values.value = primitive.defaultValue
            }
            else -> null
        }?.let { itemState ->
            // store in local registry
            _formItems[primitive.id] = itemState
            // update validation state
            isFormComplete.value = !_formItems.values.any { !it.isComplete }
        }
    }

    fun getValue(primitive: TextInputPrimitive): String {
        val item = _formItems[primitive.id] as? TextInputFormItemState
        return item?.text?.value ?: ""
    }

    fun setValue(primitive: TextInputPrimitive, value: String) {
        val item = _formItems[primitive.id] as? TextInputFormItemState
        item?.setValue(value)
        checkValidation()
    }

    fun getValue(primitive: OptionSelectPrimitive): Set<String> {
        val item = _formItems[primitive.id] as? OptionSelectFormItemState
        return item?.values?.value ?: setOf()
    }

    fun setValue(primitive: OptionSelectPrimitive, value: String) {
        val item = _formItems[primitive.id] as? OptionSelectFormItemState
        item?.setValue(value)
        checkValidation()
    }

    private fun checkValidation() {
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
                // simple case, update the one and only value
                SINGLE -> values.value = setOf(newValue)
                // more complex case, toggle item on/off in the set of selections
                MULTIPLE -> setMultipleSelectValue(newValue)
            }
        }

        private fun setMultipleSelectValue(newValue: String) {
            if (values.value.contains(newValue)) {
                // this is the case we are toggling something off - removing
                values.value = values.value.filter { it != newValue }.toSet()
            } else {
                // toggling something on...
                if (primitive.maxSelections != null) {
                    if (values.value.count().toUInt() < primitive.maxSelections) {
                        // it is OK to add, since we'll still be at or under the max
                        val updated = values.value.toMutableSet()
                        updated.add(newValue)
                        values.value = updated
                    } else {
                        // Would be selecting more than the max, so no change
                    }
                } else {
                    // no max, so fine to add
                    val updated = values.value.toMutableSet()
                    updated.add(newValue)
                    values.value = updated
                }
            }
        }
    }
}
