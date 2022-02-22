package com.appcues.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.os.bundleOf
import com.appcues.R
import com.appcues.ui.AppcuesViewModel.UIState.Completed
import com.appcues.ui.AppcuesViewModel.UIState.Render
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.trait.DialogTrait

internal class AppcuesActivity : AppCompatActivity() {

    companion object {

        private const val EXTRA_SCOPE_ID = "EXTRA_SCOPE_ID"

        fun getIntent(context: Context, scopeId: String): Intent =
            Intent(context, AppcuesActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        EXTRA_SCOPE_ID to scopeId
                    )
                )
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
    }

    private val scopeId: String by lazy { intent.getStringExtra(EXTRA_SCOPE_ID)!! }

    private val viewModel: AppcuesViewModel by viewModels { AppcuesViewModelFactory(scopeId) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onCreate(savedInstanceState)

        viewModel.uiState.observe(this) {
            when (it) {
                is Render -> {
                    setContent {
                        AppcuesTheme {
                            CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { viewModel.testAction() }) {
                                DialogTrait {
                                    it.experience.steps.first().content.Compose()
                                }
                            }
                        }
                    }
                    viewModel.onRender()
                }
                Completed -> finishAnimated()
            }
        }
    }

    private fun finishAnimated() {
        viewModel.onFinish()
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
