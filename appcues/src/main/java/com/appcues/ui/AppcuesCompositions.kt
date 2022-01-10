package com.appcues.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Sample of Composition building a static Dialog with a text and a button to close it
 */
@Composable
fun AppcuesDialog() {
    val openDialog = remember { mutableStateOf(true) }
    if (openDialog.value) {
        AppcuesView(
            dispatchTouchEvent = {},
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color(0xA0000000)),
                contentAlignment = Alignment.Center,

                ) {
                Card(
                    modifier = Modifier.defaultMinSize(minWidth = 200.dp, minHeight = 100.dp),
                    elevation = 10.dp,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(color = Color(0xFFFFFFFF)),
                    ) {
                        Text(text = "First Dialog")

                        Spacer(modifier = Modifier.height(48.dp))

                        Button(
                            onClick = {
                                openDialog.value = false
                            }) {
                            Text(text = "Close")
                        }
                    }
                }
            }
        }
    }
}
