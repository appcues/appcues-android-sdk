package com.appcues.trait.appcues

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine
import com.appcues.trait.ContentOverlayingTrait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class AppcuesSkippableTrait(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine
) : ContentOverlayingTrait {

    @Composable
    override fun Overlay(scope: BoxScope) {
        with(scope) {
            IconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        stateMachine.handleAction(EndExperience())
                    }
                }
            ) {
                Icon(painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel), contentDescription = "Close")
            }
        }
    }
}
