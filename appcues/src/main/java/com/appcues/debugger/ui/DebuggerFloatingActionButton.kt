package com.appcues.debugger.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.Icon
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.appcues.R
import com.appcues.R.drawable
import com.appcues.debugger.DebuggerViewModel
import com.appcues.ui.theme.AppcuesColors

private const val FAB_DRAGGING_SIZE_MULTIPLIER = 1.1f
private const val FAB_DEFAULT_SIZE_MULTIPLIER = 1.0f
private const val FAB_PAUSED_SIZE_MULTIPLIER = 0.0f

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BoxScope.DebuggerFloatingActionButton(
    debuggerState: MutableDebuggerState,
    debuggerViewModel: DebuggerViewModel,
) {
    val resizeBy = animateFloatAsState(
        when {
            // if its dragging and paused we diminish so it doesn't mess with current RESUMED debugger
            // important for our case where we have transparent activity on top of customer's
            // and we know we have two debuggers showing at the same time. one on top of the other.
            debuggerState.isDragging.targetState && debuggerState.isPaused.value -> FAB_PAUSED_SIZE_MULTIPLIER
            debuggerState.isDragging.targetState -> FAB_DRAGGING_SIZE_MULTIPLIER
            else -> FAB_DEFAULT_SIZE_MULTIPLIER
        }
    )
    AnimatedVisibility(
        visibleState = debuggerState.isVisible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = Modifier
            .align(Alignment.TopStart)
            // this is used along with the dynamic size of FAB Box below
            // so we create the nice effect of enlarging equally to all directions
            .offsetResize(
                offset = debuggerState.getFabPositionAsIntOffset(),
                originalSize = debuggerState.fabSize,
                resizeBy = resizeBy.value,
            )
    ) {
        val elevation = animateDpAsState(
            targetValue = when {
                debuggerState.isDragging.targetState -> 8.dp
                debuggerState.isExpanded.targetState -> 2.dp
                debuggerState.isVisible.currentState -> 4.dp
                else -> 0.dp
            }
        )

        Box(
            modifier = Modifier
                .shadow(
                    elevation = elevation.value,
                    shape = RoundedCornerShape(percent = 100)
                )
                .clickableAndDraggable(
                    onClickLabel = LocalContext.current.getString(R.string.appcues_debugger_fab_on_click_label),
                    onDragEnd = { debuggerViewModel.onDragEnd() },
                    onDrag = { debuggerViewModel.onDragging(it) },
                    onClick = { debuggerViewModel.onFabClick() }
                )
                .size(size = debuggerState.fabSize.times(resizeBy.value))
                .clip(RoundedCornerShape(percent = 100))
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(AppcuesColors.ShadyNeonBlue, AppcuesColors.PurpleAnemone)
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = drawable.appcues_ic_white_logo),
                tint = Color.White,
                contentDescription = LocalContext.current.getString(R.string.appcues_debugger_fab_image_content_description)
            )
        }
    }
}

private fun Modifier.offsetResize(offset: IntOffset, originalSize: Dp, resizeBy: Float) = then(
    Modifier.offset {
        offset
            .let {
                // get enlarged size
                val size = originalSize.times(resizeBy)
                // find out the value to subtract x and y offset from
                // the difference between original size and enlarged size divided by two
                // so we  can emulate it growing to all directions at the same time.
                val subtractOffset = ((size - originalSize) / 2)
                    .toPx()
                    .toInt()
                // generate new offset with subtracted value
                it.copy(it.x - subtractOffset, it.y - subtractOffset)
            }
    }
)

private fun Modifier.clickableAndDraggable(
    onClickLabel: String,
    onDragEnd: () -> Unit,
    onDrag: (Offset) -> Unit,
    onClick: () -> Unit,
) = then(
    composed {
        Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            )
            .pointerInput(Unit) {

                detectDragGestures(
                    onDragEnd = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount)
                    }
                )
            }
    }
)
