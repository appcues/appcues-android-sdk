package com.appcues.trait.appcues.effects

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.appcues.data.model.styling.ComponentStyle
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position.Relative
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit.SECONDS

@Composable
internal fun ConfettiEffect(style: ComponentStyle?) {
    val colors = remember { style?.colors?.map { it.toInt() } ?: listOf() }

//    val point1 = Party(
//        speed = 0f,
//        maxSpeed = 35f,
//        damping = 0.9f,
//        spread = 20,
//        size = listOf(Size(10), Size(12), Size(16)),
//        timeToLive = 5000,
//        angle = 330,
//        fadeOutEnabled = false,
//        colors = colors,
//        position = Relative(0.0, 0.2),
//        emitter = Emitter(duration = 2, SECONDS).perSecond(100)
//    )
//
//    val point2 = Party(
//        speed = 0f,
//        maxSpeed = 35f,
//        damping = 0.9f,
//        spread = 20,
//        timeToLive = 5000,
//        size = listOf(Size(10), Size(12), Size(16)),
//        angle = 210,
//        fadeOutEnabled = false,
//        colors = colors,
//        position = Relative(1.0, 0.2),
//        emitter = Emitter(duration = 2, SECONDS).perSecond(100)
//    )

    val point3 = Party(
        speed = 0f,
        maxSpeed = 60f,
        damping = 0.9f,
        spread = 180,
        size = listOf(Size(10), Size(12), Size(16)),
        angle = 270,
        timeToLive = 5000,
        fadeOutEnabled = true,
        colors = colors,
        position = Relative(0.5, -0.05),
        emitter = Emitter(duration = 2, SECONDS).perSecond(200)
    )

    KonfettiView(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(point3),
    )
}
