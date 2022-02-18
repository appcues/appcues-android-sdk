package com.appcues.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.core.os.bundleOf
import com.appcues.R
import com.appcues.data.model.Experience
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait

internal class AppcuesActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_SCOPE_ID = "EXTRA_SCOPE_ID"
        private const val EXTRA_EXPERIENCE = "EXTRA_EXPERIENCE"

        fun getIntent(context: Context, scopeId: String, experience: Experience): Intent =
            Intent(context, AppcuesActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        EXTRA_SCOPE_ID to scopeId,
                        EXTRA_EXPERIENCE to experience,
                    )
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private val scopeId: String by lazy { intent.getStringExtra(EXTRA_SCOPE_ID)!! }

    private val experience: Experience by lazy { intent.getParcelableExtra(EXTRA_EXPERIENCE)!! }

    private val viewModel: AppcuesViewModel by viewModels { AppcuesViewModelFactory(scopeId, experience) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onCreate(savedInstanceState)
        setContent {
            AppcuesTheme {
                CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { finishAnimated() }) {
                    remember { viewModel.experienceState }.run {
                        DialogTrait {
                            value.steps.first().content.Compose()
                        }
                    }
                }
            }
        }
    }

    private fun finishAnimated() {
        viewModel.finish()
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
