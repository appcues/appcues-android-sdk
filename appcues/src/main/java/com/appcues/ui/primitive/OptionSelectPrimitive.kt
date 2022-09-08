package com.appcues.ui.primitive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.styling.ComponentControlPosition
import com.appcues.data.model.styling.ComponentControlPosition.BOTTOM
import com.appcues.data.model.styling.ComponentControlPosition.HIDDEN
import com.appcues.data.model.styling.ComponentControlPosition.LEADING
import com.appcues.data.model.styling.ComponentControlPosition.TOP
import com.appcues.data.model.styling.ComponentControlPosition.TRAILING
import com.appcues.data.model.styling.ComponentDisplayFormat.HORIZONTAL_LIST
import com.appcues.data.model.styling.ComponentDisplayFormat.PICKER
import com.appcues.data.model.styling.ComponentSelectMode
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.data.model.styling.ComponentSelectMode.SINGLE
import com.appcues.data.model.styling.ComponentStyle
import com.appcues.ui.LocalExperienceStepFormStateDelegate
import com.appcues.ui.extensions.getColor
import com.appcues.ui.extensions.getHorizontalAlignment
import com.appcues.ui.extensions.styleBorder

@Composable
internal fun OptionSelectPrimitive.Compose(modifier: Modifier) {
    val formState = LocalExperienceStepFormStateDelegate.current
    val selectedValues = remember { mutableStateOf(formState.getValue(this)) }

    fun setSelectedValues(values: Set<String>) {
        selectedValues.value = values
        formState.setValue(this, values)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = style.getHorizontalAlignment(),
    ) {

        // the form item label / question
        label.Compose()

        when {
            selectMode == SINGLE && displayFormat == PICKER -> {
                options.ComposePicker(
                    selectedValues = selectedValues.value,
                    modifier = Modifier.styleBorder(pickerStyle ?: ComponentStyle(), isSystemInDarkTheme()),
                    placeholder = placeholder,
                    accentColor = accentColor?.getColor(isSystemInDarkTheme()),
                ) {
                    setSelectedValues(it)
                }
            }
            displayFormat == HORIZONTAL_LIST -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    options.ComposeSelections(
                        selectedValues = selectedValues.value,
                        selectMode = selectMode,
                        controlPosition = controlPosition,
                        selectedColor = selectedColor.getColor(isSystemInDarkTheme()),
                        unselectedColor = unselectedColor.getColor(isSystemInDarkTheme()),
                        accentColor = accentColor.getColor(isSystemInDarkTheme()),
                    ) {
                        setSelectedValues(it)
                    }
                }
            }
            else -> { // VERTICAL_LIST case or a fallback (i.e. a PICKER but with multi-select, invalid)
                Column(horizontalAlignment = Alignment.Start) {
                    options.ComposeSelections(
                        selectedValues = selectedValues.value,
                        selectMode = selectMode,
                        controlPosition = controlPosition,
                        selectedColor = selectedColor.getColor(isSystemInDarkTheme()),
                        unselectedColor = unselectedColor.getColor(isSystemInDarkTheme()),
                        accentColor = accentColor.getColor(isSystemInDarkTheme()),
                    ) {
                        setSelectedValues(it)
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
    selectedColor: Color?,
    unselectedColor: Color?,
    accentColor: Color?,
    valueSelectionChanged: (Set<String>) -> Unit,
) {
    forEach { option ->

        fun updateSelection(value: String, selected: Boolean) {
            when (selectMode) {
                SINGLE -> {
                    // in single select (radio), you cannot deselect an item
                    // only select a new one.
                    if (selected) {
                        val set = mutableSetOf(value)
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

        val isSelected = selectedValues.contains(option.value)
        val contentView by remember(isSelected) {
            derivedStateOf {
                option.selectedContent?.let { if (isSelected) it else option.content } ?: option.content
            }
        }

        when (controlPosition) {
            LEADING -> Row(verticalAlignment = Alignment.CenterVertically) {
                selectMode.Compose(isSelected, selectedColor, unselectedColor, accentColor) { updateSelection(option.value, !isSelected) }
                contentView.Compose()
            }
            TRAILING -> Row(verticalAlignment = Alignment.CenterVertically) {
                contentView.Compose()
                selectMode.Compose(isSelected, selectedColor, unselectedColor, accentColor) { updateSelection(option.value, !isSelected) }
            }
            TOP -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                selectMode.Compose(isSelected, selectedColor, unselectedColor, accentColor) { updateSelection(option.value, !isSelected) }
                contentView.Compose()
            }
            BOTTOM -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                contentView.Compose()
                selectMode.Compose(isSelected, selectedColor, unselectedColor, accentColor) { updateSelection(option.value, !isSelected) }
            }
            HIDDEN -> Box(modifier = Modifier.clickable { updateSelection(option.value, !isSelected) }) {
                contentView.Compose()
            }
        }
    }
}

@Composable
private fun ComponentSelectMode.Compose(
    selected: Boolean,
    selectedColor: Color?,
    unselectedColor: Color?,
    accentColor: Color?,
    selectionToggled: () -> Unit,
) {
    when (this) {
        SINGLE -> {
            RadioButton(
                selected = selected,
                onClick = selectionToggled,
                colors = RadioButtonDefaults.colors(
                    // the builder should always send these values, but default to the theme like the standard default behavior
                    selectedColor = selectedColor ?: MaterialTheme.colors.secondary,
                    unselectedColor = unselectedColor ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                )
            )
        }
        MULTIPLE -> {
            Checkbox(
                checked = selected,
                onCheckedChange = { selectionToggled() },
                colors = CheckboxDefaults.colors(
                    // the builder should always send these values, but default to the theme like the standard default behavior
                    checkedColor = selectedColor ?: MaterialTheme.colors.secondary,
                    uncheckedColor = unselectedColor ?: MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                    checkmarkColor = accentColor ?: MaterialTheme.colors.surface,
                )
            )
        }
    }
}

@Composable
private fun List<OptionSelectPrimitive.OptionItem>.ComposePicker(
    selectedValues: Set<String>,
    modifier: Modifier,
    placeholder: ExperiencePrimitive?,
    accentColor: Color?,
    valueSelectionChanged: (Set<String>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedValue = selectedValues.firstOrNull()
    Box(modifier = Modifier.clickable(onClick = { expanded = true })) {
        // 1. render the selected item as the collapsed state
        Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // we should always either have a selected item, or a placeholder, or this will just be a blank
            // box with a dropdown arrow
            if (selectedValue != null) {
                // if we have a selected value, we assume one of our options will match it, and compose it
                this@ComposePicker.firstOrNull { it.value == selectedValue }?.let {
                    // show selectedContent if available, since this is the selected item, else default content
                    (it.selectedContent ?: it.content).Compose()
                }
            } else {
                // no selection, render a placeholder, if exists
                placeholder?.Compose()
            }
            Spacer(modifier = Modifier.weight(1.0f))
            Icon(
                modifier = Modifier
                    .padding(all = 14.dp)
                    .size(size = 20.dp),
                contentDescription = null,
                imageVector = ImageVector.vectorResource(id = R.drawable.appcues_ic_drop_down),
                tint = accentColor ?: MaterialTheme.colors.secondary,
            )
        }
        // 2. the dropdown menu for selection that shows on expanded state
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            forEach { optionItem ->
                val isSelected = selectedValue == optionItem.value
                val contentView by remember(isSelected) {
                    derivedStateOf {
                        optionItem.selectedContent?.let { if (isSelected) it else optionItem.content } ?: optionItem.content
                    }
                }
                DropdownMenuItem(onClick = {
                    expanded = false
                    valueSelectionChanged(setOf(optionItem.value))
                }) {
                    contentView.Compose()
                }
            }
        }
    }
}
