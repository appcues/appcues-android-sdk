package com.appcues.trait.appcues.effects

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.appcues.data.mapper.styling.mapToColors
import com.appcues.data.model.styling.ComponentStyle
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position.Relative
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit.MILLISECONDS

private const val EMITTER_INTENSITY_RAW = 200

@Composable
internal fun ConfettiEffect(style: ComponentStyle?, duration: Int, intensity: Double) {
    // default colors in case colors style property is empty
    val colors = remember {
        val styleColors = style?.colors ?: listOf()
        
        styleColors.ifEmpty { listOf("#5C5CFF", "#20E0D6", "#FF5290").mapToColors() }.map { it.toInt() }
    }

    // this is a center point on the top of the screen, that will disperse particles
    // in a 180 angle spread before it start falling straight down
    val party = Party(
        speed = 0f,
        maxSpeed = 60f,
        damping = 0.9f,
        spread = 180,
        size = listOf(Size(sizeInDp = 10), Size(sizeInDp = 12), Size(sizeInDp = 16)),
        angle = 270,
        timeToLive = 5000,
        fadeOutEnabled = true,
        colors = colors,
        position = Relative(x = 0.5, y = -0.05),
        emitter = Emitter(duration = duration.toLong(), MILLISECONDS).perSecond((EMITTER_INTENSITY_RAW * intensity).toInt())
    )

    KonfettiView(
        modifier = Modifier.fillMaxSize(),
        parties = listOf(party),
    )
}
