package com.appcues.trait.appcues

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine
import com.appcues.trait.BackdropDecoratingTrait
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class AppcuesBackdropTrait(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine
) : BackdropDecoratingTrait {

    @Composable
    override fun Backdrop(scope: BoxScope) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                // background for test
                .background(color = Color(color = 0xA0000000))
                // add click listener but without any ripple effect
                .pointerInput(Unit) {
                    detectTapGestures {
                        CoroutineScope(Dispatchers.Main).launch {
                            stateMachine.handleAction(EndExperience())
                        }
                    }
                },
        )
    }
}
