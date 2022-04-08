package com.appcues.ui.debugger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.appcues.R.drawable

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BoxScope.DebuggerFloatingActionButton(
    debuggerState: MutableDebuggerState,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visibleState = debuggerState.isVisible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset { debuggerState.getFabPositionAsIntOffset() }
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(percent = 100),
                    ambientColor = Color(color = 0xFF5C5CFF),
                    spotColor = Color(color = 0xFF000000)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClickLabel = "Appcues debugger details",
                    role = Role.Button,
                    onClick = onClick
                )
                .pointerInput(Unit) {

                    detectDragGestures(
                        onDragStart = { onDragStart() },
                        onDragEnd = { onDragEnd() },
                        onDrag = { change, dragAmount ->
                            change.consumeAllChanges()
                            onDrag(dragAmount)
                        }
                    )
                }
                .size(size = debuggerState.fabSize)
                .clip(RoundedCornerShape(percent = 100))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(color = 0xFF5C5CFF), Color(color = 0xFFFF92C6)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Image(painter = painterResource(id = drawable.ic_debugger_appcues_logo), contentDescription = "Logo debugger")
        }
    }
}
