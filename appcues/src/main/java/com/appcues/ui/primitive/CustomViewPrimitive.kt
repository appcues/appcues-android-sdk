package com.appcues.ui.primitive

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.appcues.data.model.ExperiencePrimitive.CustomComponentPrimitive
import com.appcues.ui.AppcuesCustomComponentDirectory
import com.appcues.ui.composables.LocalViewModel

@Composable
internal fun CustomComponentPrimitive.Compose(modifier: Modifier) {
    val customComponent = AppcuesCustomComponentDirectory.get(identifier) ?: return

    val viewModel = LocalViewModel.current
    val controller = remember { viewModel.getRemoteController() }

    AndroidView(modifier = modifier, factory = { customComponent.getView(controller, config) })
}
