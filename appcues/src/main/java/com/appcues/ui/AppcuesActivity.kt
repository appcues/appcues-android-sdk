package com.appcues.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.ui.extensions.ComposeExperience
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait

internal class AppcuesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppcuesTheme {
                DialogTrait {
                    experienceModalOne.ComposeExperience(onClick = { finish() })
                }
            }
        }
    }

    @Preview(
        name = "First Preview",
        showBackground = true
    )
    @Composable
    fun Preview() {
        AppcuesTheme {
            DialogTrait {
                experienceModalOne.ComposeExperience(onClick = { finish() })
            }
        }
    }
}
