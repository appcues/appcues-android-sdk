package com.appcues.ui

import androidx.compose.runtime.mutableStateOf
import java.util.UUID

internal class ExperienceStepFormState {
    private var formItems: HashMap<UUID, ExperienceStepFormItem> = hashMapOf()

    // this can be used by buttons needing to know if they are enabled or not based on required input
    val isFormComplete = mutableStateOf(true)

    fun captureFormItem(id: UUID, item: ExperienceStepFormItem) {
        formItems[id] = item
        isFormComplete.value = !formItems.values.any { !it.isComplete }
    }
}

internal sealed class ExperienceStepFormItem(open val label: String, open val isRequired: Boolean) {

    val isComplete: Boolean
        get() {
            return when (this) {
                is MultipleTextFormItem -> !isRequired || values.isNotEmpty()
                is SingleTextFormItem -> !isRequired || value.isNotBlank()
            }
        }

    class SingleTextFormItem(
        override val label: String,
        override val isRequired: Boolean,
        var value: String,
    ) : ExperienceStepFormItem(label, isRequired)

    class MultipleTextFormItem(
        override val label: String,
        override val isRequired: Boolean,
        val values: Set<String>
    ) : ExperienceStepFormItem(label, isRequired)
}
