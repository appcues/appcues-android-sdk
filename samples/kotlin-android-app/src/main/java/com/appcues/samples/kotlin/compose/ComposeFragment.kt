package com.appcues.samples.kotlin.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appcues.samples.kotlin.ExampleApplication

@Composable
fun ComposeFragment() {
    val appcues = ExampleApplication.appcues
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Compose!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
        
        Text(
            text = "This is a Jetpack Compose fragment",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = {
                appcues.track("event1")
            },
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("Trigger Event 1")
        }

        Button(
            onClick = {
                // Finish the activity to go back to the main activity
                (context as? androidx.activity.ComponentActivity)?.finish()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Back to Main")
        }
    }
}
