package com.appcues.trait.appcues

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.StateMachine
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait

internal class SkippableTrait(
    override val config: AppcuesConfigMap,
    private val stateMachine: StateMachine
) : ContainerDecoratingTrait, BackdropDecoratingTrait {

    @Composable
    override fun BoxScope.Overlay() {
        IconButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .clip(CircleShape)
                .size(30.dp, 30.dp),
            onClick = {
                stateMachine.handleAction(EndExperience)
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_dismiss),
                contentDescription = stringResource(id = R.string.skippable_trait_dismiss),
            )
        }
    }

    @Composable
    override fun BoxScope.Backdrop() {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                // add click listener but without any ripple effect, should this happen?
                .pointerInput(Unit) {
                    detectTapGestures {
                        stateMachine.handleAction(EndExperience)
                    }
                },
        )
    }
}
