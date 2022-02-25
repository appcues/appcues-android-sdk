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
import com.appcues.trait.appcues.AppcuesModalTrait
import com.appcues.ui.AppcuesViewModel.UIState.Completed
import com.appcues.ui.AppcuesViewModel.UIState.Render
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme

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

        viewModel.uiState.observe(this) { state ->
            when (state) {
                is Render -> {
                    // noticed that for some reason something is triggering a recomposition.
                    // its not impacting anything but it would be good to find out why.
                    setContent {
                        AppcuesTheme {
                            CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { viewModel.onAction(it) }) {
                                // later this will be done from the view model state object
                                AppcuesModalTrait(null, viewModel.stateMachine, applicationContext, scopeId).WrapContent {
                                    state.experience.steps.first().content.Compose()
                                }
                            }
                        }
                    }
                    viewModel.onRender()
                }
                Completed -> finish()
            }
        }
    }

    override fun finish() {
        super.finish()
        viewModel.onEndExperience()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
