package com.appcues.ui.debugger

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.appcues.R.drawable

@Composable
internal fun BoxScope.DismissDebugger(isDragging: MutableTransitionState<Boolean>, dismissRect: MutableState<Rect>) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomCenter),
        visibleState = isDragging,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        with(LocalDensity.current) {
            Box(
                modifier = Modifier
                    .background(brush = Brush.radialGradient(listOf(Color(color = 0x40000000), Color(color = 0x00000000))))
                    .size(168.dp)
                    .onGloballyPositioned {
                        dismissRect.value =
                            Rect(it.positionInRoot(), Size(it.size.width.toFloat(), it.size.height.toFloat())).deflate(28.dp.toPx())
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = drawable.ic_dismiss),
                    modifier = Modifier.clip(RoundedCornerShape(percent = 100)),
                    contentDescription = "Dismiss debugger"
                )
            }
        }
    }
}
