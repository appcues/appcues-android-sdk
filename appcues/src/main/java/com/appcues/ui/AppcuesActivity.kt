package com.appcues.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.appcues.R
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait

internal class AppcuesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppcuesTheme {
                CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { finishAnimated() }) {
                    DialogTrait {
                        experienceModalOne.Compose()
                    }
                }
            }
        }
    }

    private fun finishAnimated() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    @Preview(
        name = "First Preview",
        showBackground = true
    )
    @Composable
    fun Preview() {
        AppcuesTheme {
            DialogTrait {
                experienceModalOne.Compose()
            }
        }
    }
}
