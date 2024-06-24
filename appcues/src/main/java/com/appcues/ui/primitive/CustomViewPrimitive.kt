package com.appcues.ui.primitive

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.appcues.data.model.ExperiencePrimitive.CustomFramePrimitive
import com.appcues.ui.composables.LocalCustomFrameDirectory
import com.appcues.ui.composables.LocalViewModel

@Composable
internal fun CustomFramePrimitive.Compose(modifier: Modifier) {
    val viewDirectory = LocalCustomFrameDirectory.current
    val viewModel = LocalViewModel.current

    val controller = remember { viewModel.getRemoteController() }

    with(viewDirectory) {
        Box(modifier = modifier) {
            ComposeView(identifier = identifier, controller = controller, config = config)
        }
    }
}
