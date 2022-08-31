package com.appcues.ui.primitive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentControlPosition.BOTTOM
import com.appcues.data.model.styling.ComponentControlPosition.HIDDEN
import com.appcues.data.model.styling.ComponentControlPosition.LEADING
import com.appcues.data.model.styling.ComponentControlPosition.TOP
import com.appcues.data.model.styling.ComponentControlPosition.TRAILING
import com.appcues.data.model.styling.ComponentDisplayFormat.HORIZONTAL_LIST
import com.appcues.data.model.styling.ComponentDisplayFormat.PICKER
import com.appcues.data.model.styling.ComponentDisplayFormat.VERTICAL_LIST
import com.appcues.data.model.styling.ComponentSelectMode
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.data.model.styling.ComponentSelectMode.SINGLE
import com.appcues.ui.ExperienceStepFormItem.MultipleTextFormItem
import com.appcues.ui.ExperienceStepFormItem.SingleTextFormItem
import com.appcues.ui.LocalExperienceStepFormStateDelegate
import com.appcues.ui.extensions.getHorizontalAlignment

@Composable
internal fun OptionSelectPrimitive.Compose(modifier: Modifier) {
    val formState = LocalExperienceStepFormStateDelegate.current
    val selectedValues = remember { mutableStateOf(defaultValue) }

    LaunchedEffect(key1 = selectedValues.value) {
        val formItem = when (selectMode) {
            MULTIPLE -> MultipleTextFormItem(label.text, required, selectedValues.value)
            SINGLE -> SingleTextFormItem(label.text, required, selectedValues.value.firstOrNull() ?: "")
        }
        formState.captureFormItem(this@Compose.id, formItem)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = style.getHorizontalAlignment(),
    ) {

        // the form item label / question
        label.Compose()

        when {
            selectMode == SINGLE && displayFormat == PICKER -> {
                options.ComposePicker(selectedValues = selectedValues.value) {
                    selectedValues.value = it
                }
            }
            displayFormat == HORIZONTAL_LIST -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    options.ComposeSelections(
                        selectedValues = selectedValues.value,
                        selectMode = selectMode,
                        controlPosition = controlPosition
                    ) {
                        selectedValues.value = it
                    }
                }
            }
            displayFormat == VERTICAL_LIST -> {
                Column(horizontalAlignment = Alignment.Start) {
                    options.ComposeSelections(
                        selectedValues = selectedValues.value,
                        selectMode = selectMode,
                        controlPosition = controlPosition
                    ) {
                        selectedValues.value = it
                    }
                }
            }
        }
    }
}

@Composable
private fun List<OptionSelectPrimitive.OptionItem>.ComposeSelections(
    selectedValues: Set<String>,
    selectMode: ComponentSelectMode,
    controlPosition: ComponentControlPosition,
    valueSelectionChanged: (Set<String>) -> Unit,
) {
    forEach {

        fun updateSelection(value: String, selected: Boolean) {
            when (selectMode) {
                SINGLE -> {
                    // in single select (radio), you cannot deselect an item
                    // only select a new one.
                    if (selected) {
                        val set = mutableSetOf<String>(value)
                        valueSelectionChanged(set)
                    }
                }
                MULTIPLE -> {
                    val set = selectedValues.toMutableSet()
                    if (selected) set.add(value) else set.remove(value)
                    valueSelectionChanged(set)
                }
            }
        }

        val isSelected = selectedValues.contains(it.value)

        when (controlPosition) {
            LEADING -> Row(verticalAlignment = Alignment.CenterVertically) {
                selectMode.Compose(selected = isSelected) { updateSelection(it.value, !isSelected) }
                it.content.Compose()
            }
            TRAILING -> Row(verticalAlignment = Alignment.CenterVertically) {
                it.content.Compose()
                selectMode.Compose(selected = isSelected) { updateSelection(it.value, !isSelected) }
            }
            TOP -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                selectMode.Compose(selected = isSelected) { updateSelection(it.value, !isSelected) }
                it.content.Compose()
            }
            BOTTOM -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                it.content.Compose()
                selectMode.Compose(selected = isSelected) { updateSelection(it.value, !isSelected) }
            }
            HIDDEN -> Box(modifier = Modifier.clickable { updateSelection(it.value, !isSelected) }) {
                it.content.Compose()
            }
        }
    }
}

@Composable
private fun ComponentSelectMode.Compose(
    selected: Boolean,
    selectionToggled: () -> Unit,
) {
    when (this) {
        SINGLE -> {
            RadioButton(
                selected = selected,
                onClick = selectionToggled,
                // TBD: how will builder supply desired styling for colors
                colors = RadioButtonDefaults.colors(
                    selectedColor = Color.DarkGray,
                    unselectedColor = Color.DarkGray,
                )
            )
        }
        MULTIPLE -> {
            Checkbox(
                checked = selected,
                onCheckedChange = { selectionToggled() },
                // TBD: how will builder supply desired styling for colors
                colors = CheckboxDefaults.colors(
                    checkedColor = Color.DarkGray,
                    uncheckedColor = Color.DarkGray,
                    checkmarkColor = Color.White,
                )
            )
        }
    }
}

@Composable
private fun List<OptionSelectPrimitive.OptionItem>.ComposePicker(
    selectedValues: Set<String>,
    valueSelectionChanged: (Set<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.clickable(onClick = { expanded = true })) {
        // 1. render the selected item as the collapsed state
        // TBD: what if no selected value? some placeholder?
        // design pending
        selectedValues.firstOrNull()?.let { selectedValue ->
            this@ComposePicker.firstOrNull { it.value == selectedValue }?.content?.Compose()
        }
        // 2. the dropdown menu for selection that shows on expanded state
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            forEach {
                DropdownMenuItem(onClick = {
                    expanded = false
                    valueSelectionChanged(setOf(it.value))
                }) {
                    it.content.Compose()
                }
            }
        }
    }
}
