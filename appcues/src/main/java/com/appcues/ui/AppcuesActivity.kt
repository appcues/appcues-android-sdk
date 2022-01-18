package com.appcues.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.ui.button.GradientTextButton
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.glide.GlideImage

internal class AppcuesActivity : AppCompatActivity() {

    companion object {

        const val SCREEN_PADDING = 0.05
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val imageUrl = "https://res.cloudinary.com/dnjrorsut/image/upload/v1635971825/98227/oh5drlvojb1spaetc1ol.jpg"
        setContent {
            MaterialTheme {
                DialogTrait {
                    GlideImage(
                        imageModel = imageUrl,
                        modifier = Modifier.animateContentSize(),
                        contentScale = ContentScale.FillWidth,
                        shimmerParams = ShimmerParams(
                            baseColor = MaterialTheme.colors.background,
                            highlightColor = MaterialTheme.colors.secondaryVariant,
                            durationMillis = 350,
                            dropOff = 0.65f,
                            tilt = 20f
                        ),
                    )

                    Text(
                        text = "Ready to make your\nworkflow simpler?",
                        modifier = Modifier.padding(top = 20.dp, bottom = 5.dp),
                        color = Color(android.graphics.Color.parseColor("#394455")),
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Take a few moments to learn how to best use our features.",
                        modifier = Modifier.padding(start = 30.dp, top = 10.dp, end = 30.dp, bottom = 15.dp),
                        color = Color(android.graphics.Color.parseColor("#394455")),
                        fontSize = 17.sp,
                        textAlign = TextAlign.Center,
                    )

                    GradientTextButton(
                        gradient = Brush.horizontalGradient(
                            listOf(
                                Color(
                                    android.graphics.Color.parseColor("#5C5CFF")
                                ),
                                Color(
                                    android.graphics.Color.parseColor("#8960FF"),
                                )
                            )
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 8.dp),
                        onClick = { finish() },
                        text = "Button 1",
                        textColor = Color(android.graphics.Color.parseColor("#ffffff")),
                        textSize = 17.sp,
                    )
                }
            }
        }
    }

    @Composable
    fun DialogTrait(content: @Composable () -> Unit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(color = 0xA0000000)),
            contentAlignment = Alignment.Center,
        ) {

            val configuration = LocalConfiguration.current
            val dialogHorizontalMargin = (configuration.screenWidthDp * SCREEN_PADDING).dp
            val dialogVerticalMargin = (configuration.screenHeightDp * SCREEN_PADDING).dp

            Card(
                modifier = Modifier
                    .defaultMinSize(minWidth = 200.dp, minHeight = 100.dp)
                    .padding(horizontal = dialogHorizontalMargin, vertical = dialogVerticalMargin),
                contentColor = Color(color = 0xFFFFFFFF),
                elevation = 10.dp,
                shape = RoundedCornerShape(12.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    content()
                }
            }
        }
    }
}
