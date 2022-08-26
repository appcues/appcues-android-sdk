package com.appcues.ui.primitive

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.appcues.R
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
import com.appcues.ui.extensions.getHorizontalAlignment

@Composable
internal fun OptionSelectPrimitive.Compose(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = style.getHorizontalAlignment(),
    ) {
        label.Compose()

        when {
            selectMode == SINGLE && displayFormat == PICKER -> {
                var expanded by remember { mutableStateOf(false) }
                var selectedIndex by remember { mutableStateOf(0) }

                Box(modifier = Modifier.clickable(onClick = { expanded = true })) {
                    options[selectedIndex].content.Compose()
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .background(Color.White)
                    ) {
                        options.forEachIndexed { index, optionItem ->
                            DropdownMenuItem(onClick = {
                                selectedIndex = index
                                expanded = false
                            }) {
                                optionItem.content.Compose()
                            }
                        }
                    }
                }
            }
            displayFormat == HORIZONTAL_LIST -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    options.Compose(selected = false, selectMode = selectMode, controlPosition = controlPosition)
                }
            }
            displayFormat == VERTICAL_LIST -> {
                Column(horizontalAlignment = Alignment.Start) {
                    options.Compose(selected = false, selectMode = selectMode, controlPosition = controlPosition)
                }
            }
        }
    }
}

@Composable
private fun List<OptionSelectPrimitive.OptionItem>.Compose(
    selected: Boolean,
    selectMode: ComponentSelectMode,
    controlPosition: ComponentControlPosition
) {
    forEach {

        var isSelected by remember { mutableStateOf(selected) }

        val selectionToggle = Modifier.clickable { isSelected = !isSelected }

        when (controlPosition) {
            LEADING -> Row(modifier = selectionToggle) {
                selectMode.Compose(selected = isSelected)
                it.content.Compose()
            }
            TRAILING -> Row(modifier = selectionToggle) {
                it.content.Compose()
                selectMode.Compose(selected = isSelected)
            }
            TOP -> Column(modifier = selectionToggle) {
                selectMode.Compose(selected = isSelected)
                it.content.Compose()
            }
            BOTTOM -> Column(modifier = selectionToggle) {
                it.content.Compose()
                selectMode.Compose(selected = isSelected)
            }
            HIDDEN -> Box(modifier = selectionToggle) {
                it.content.Compose()
            }
        }
    }
}

@Composable
private fun ComponentSelectMode.Compose(
    selected: Boolean
) {
    val resId = when (this) {
        SINGLE -> when (selected) {
            true -> R.drawable.appcues_ic_radio_button_selected
            false -> R.drawable.appcues_ic_radio_button_unselected
        }
        MULTIPLE -> when (selected) {
            true -> R.drawable.appcues_ic_check_box_selected
            false -> R.drawable.appcues_ic_check_box_unselected
        }
    }

    Image(
        painter = painterResource(id = resId), 
        contentDescription = null,
        modifier = Modifier.zIndex(1f)
    )
}
