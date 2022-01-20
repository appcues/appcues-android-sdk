package com.appcues.ui.trait

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

private const val SCREEN_PADDING = 0.05

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
