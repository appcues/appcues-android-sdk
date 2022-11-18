package com.appcues.trait.appcues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import com.appcues.data.model.AppcuesConfigMap
import com.appcues.trait.BackdropDecoratingTrait

internal class KeyholeTrait(
    override val config: AppcuesConfigMap,
) : BackdropDecoratingTrait {

    companion object {

        const val TYPE = "@appcues/keyhole"
    }

    @Composable
    override fun BoxScope.BackdropDecorate(content: @Composable BoxScope.() -> Unit) {
        // temporary block of code before we understand how to style keyhole
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .fillMaxSize()
                // set background color
                .drawWithContent {
                    val outline =
                        RoundedCornerShape(percent = 100).createOutline(Size(width = 400f, height = 400f), layoutDirection, density)
                    clipPath(
                        path = Path().apply {
                            addOutline(outline)
                            translate(Offset(x = 200f, y = 80f))
                        },
                        clipOp = ClipOp.Difference,
                    ) {
                        this@drawWithContent.drawContent()
                    }

                    //                    drawOval(
                    //                        brush = Brush.radialGradient(
                    //                            center = Offset(400f, 280f),
                    //                            radius = 205f,
                    //                            colors = listOf(Color(0x00000000), Color(0x00000000), Color(0x00000000), Color(0xFFFF5050)),
                    //                            tileMode = TileMode.Clamp
                    //                        ),
                    //                        topLeft = Offset(200f, 80f),
                    //                        size = Size(400f, 400f)
                    //                    )
                }

        ) {
            content()
        }
    }
}
