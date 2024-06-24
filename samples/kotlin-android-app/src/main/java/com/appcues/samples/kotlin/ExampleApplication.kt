package com.appcues.samples.kotlin

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.appcues.Appcues
import com.appcues.AppcuesComposeView
import com.appcues.ExperienceRemoteController
import com.appcues.LoggingLevel
import com.appcues.NavigationHandler
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class ExampleApplication : Application() {

    companion object {

        lateinit var appcues: Appcues
        var currentUserID = "default-0000"
    }

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build()
            )

            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build()
            )
        }

        appcues = Appcues(this, BuildConfig.APPCUES_ACCOUNT_ID, BuildConfig.APPCUES_APPLICATION_ID) {
            loggingLevel = if (BuildConfig.DEBUG) LoggingLevel.DEBUG else LoggingLevel.INFO
            navigationHandler = object : NavigationHandler {
                // This is an example where we're processing navigation requests coming from Appcues experiences,
                // but simply forwarding them on to start an Activity from an Intent with the given Uri. If an application
                // had a more sophisticated deep linking mechanism and needed finer control over reporting the completion
                // of link navigation - this is where the SDK would allow hooking in and supplying that.
                override suspend fun navigateTo(uri: Uri): Boolean {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = uri
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also {
                        startActivity(it)
                    }
                    return true // navigation successful
                }
            }
        }

        appcues.registerCustomFrame("youtubeVideo", YoutubeVideoView())
        appcues.registerCustomFrame("setTheme", SetThemeView())
    }

    private class SetThemeView : AppcuesComposeView {

        @Composable
        override fun BoxScope.Compose(controller: ExperienceRemoteController, config: Map<String, Any>?) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {
                        controller.close()

                        val mainHandler = Handler(Looper.getMainLooper());

                        mainHandler.postDelayed({
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        }, 300)

                    }) {
                        Text(text = "Dark Mode")
                    }
                    Button(onClick = {
                        controller.close()

                        val mainHandler = Handler(Looper.getMainLooper());

                        mainHandler.postDelayed({
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }, 300)

                    }) {
                        Text(text = "Light Mode")
                    }
                }
            }
        }
    }

    private class YoutubeVideoView : AppcuesComposeView {

        @Composable
        override fun BoxScope.Compose(controller: ExperienceRemoteController, config: Map<String, Any>?) {
            val isAfterBuzz = remember { mutableStateOf(false) }

            Column {
                AndroidView(
                    factory = {
                        val view = YouTubePlayerView(it).apply {
                            enableBackgroundPlayback(false)
                            addYouTubePlayerListener(
                                object : AbstractYouTubePlayerListener() {
                                    override fun onReady(youTubePlayer: YouTubePlayer) {
                                        super.onReady(youTubePlayer)
                                        youTubePlayer.loadVideo("IOxfD1y6cDM", 0f)
                                    }

                                    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                                        if (second > 9f && isAfterBuzz.value.not()) {
                                            isAfterBuzz.value = true
                                        }
                                    }
                                }
                            )
                        }
                        view
                    }
                )

                AnimatedVisibility(visible = isAfterBuzz.value) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Tell us how crazy that play was")

                        val radioOptions = listOf("Ok", "Nice", "AMAZING!")
                        val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[1]) }
                        Row {
                            radioOptions.forEach { text ->
                                Column(
                                    Modifier
                                        .selectable(
                                            selected = (text == selectedOption),
                                            onClick = {
                                                onOptionSelected(text)
                                            }
                                        )
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    RadioButton(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) }
                                    )
                                    Text(
                                        text = text,
                                        style = MaterialTheme.typography.body1.merge(),
                                    )
                                }
                            }
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(color = 0xFF007A33)),
                            onClick = {
                                controller.submitForm("buzzer_play", mapOf("how_crazy_was_that_play" to selectedOption))
                                controller.nextStep()
                            }) {
                            Text(color = Color.White, text = "Send")
                        }
                    }

                }

            }
        }
    }
}
