package com.appcues.ui.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.TextUnit

@Composable
fun GradientTextButton(
    gradient: Brush,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    shape: Shape = MaterialTheme.shapes.small,
    text: String,
    textColor: Color = Color.Unspecified,
    textSize: TextUnit = TextUnit.Unspecified,
) {
    Button(
        modifier = modifier,
        shape = shape,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(),
        onClick = onClick,
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .then(modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = textSize,
            )
        }
    }
}
