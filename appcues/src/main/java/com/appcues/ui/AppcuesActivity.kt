package com.appcues.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import com.appcues.R
import com.appcues.di.AppcuesKoinContext
import com.appcues.ui.AppcuesViewModel.UIAction
import com.appcues.ui.AppcuesViewModel.UIAction.Finish
import com.appcues.ui.AppcuesViewModel.UIState.Render
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.scope.Scope

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

    private val scope: Scope by lazy { AppcuesKoinContext.koin.getScope(intent.getStringExtra(EXTRA_SCOPE_ID)!!) }

    private val viewModel: AppcuesViewModel by viewModels { AppcuesViewModelFactory(scope) }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        super.onCreate(savedInstanceState)

        setContent {
            AppcuesActivityContent()
        }

        // handle in a separate stream ui actions
        viewModel.uiAction.handleActions()
    }

    @Composable
    private fun AppcuesActivityContent() {
        AppcuesTheme {
            CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { viewModel.onAction(it) }) {
                // observe ui state
                val state = viewModel.uiState.collectAsState().value
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    // if state is Render, then we compose it
                    if (state is Render) {
                        state.Compose(boxScope = this)
                    }
                }
            }
        }
    }

    @Composable
    private fun Render.Compose(boxScope: BoxScope) {
        // show if render state is not null
        val step = stepContainer.steps.getOrNull(position)

        with(stepContainer) {
            // apply backdrop traits
            backdropTraits.forEach { it.Backdrop(scope = boxScope) }
            // create wrapper
            contentWrappingTrait.WrapContent {
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        // Compose content (primitives)
                        step?.content?.Compose()
                    }

                    // Compose all container traits on top of the content Column
                    containerTraits.forEach { it.Overlay(scope = this) }
                }
            }
        }
    }

    private fun SharedFlow<UIAction>.handleActions() {
        lifecycleScope.launch {
            collect { action ->
                when (action) {
                    Finish -> handleFinishAction()
                }
            }
        }
    }

    private fun handleFinishAction() {
        finish()
    }

    override fun finish() {
        super.finish()
        viewModel.onEndExperience()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}
