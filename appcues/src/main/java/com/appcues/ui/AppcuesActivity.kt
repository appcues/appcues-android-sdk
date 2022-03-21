package com.appcues.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.os.bundleOf
import com.appcues.di.AppcuesKoinContext
import com.appcues.ui.AppcuesViewModel.UIState
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.extensions.Compose
import com.appcues.ui.theme.AppcuesTheme
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
        // remove enter animation from this activity
        overridePendingTransition(0, 0)
        super.onCreate(savedInstanceState)

        setContent {
            AppcuesTheme {
                Composition()
            }
        }
    }

    @Composable
    private fun Composition() {
        CompositionLocalProvider(LocalAppcuesActions provides AppcuesActions { viewModel.onAction(it) }) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                // collect all UIState
                viewModel.uiState.collectAsState().let {
                    // update and compose last rendering state remember based on latest UIState
                    ComposeLastRenderingState(it.value, this)

                    // will run when transition from visible to gone is completed
                    LaunchOnHideAnimationCompleted {
                        // if state is dismissing then finish activity
                        if (it.value is Dismissing) finish()
                    }
                }
            }
        }
    }

    @Composable
    private fun ComposeLastRenderingState(
        state: UIState,
        boxScope: BoxScope,
    ) {
        remember { mutableStateOf<Rendering?>(null) }
            .let {
                it.value = if (state is Rendering) {
                    // if UIState is rendering then we set new value and show content
                    isContentVisible.targetState = true
                    state
                } else {
                    // else we keep the same value and hide content
                    isContentVisible.targetState = false
                    it.value
                }
                // return Rendering?
                it.value
            }?.Compose(boxScope = boxScope)
    }

    @Composable
    private fun Rendering.Compose(boxScope: BoxScope) {
        with(stepContainer) {
            // apply backdrop traits
            backdropTraits.forEach { it.run { boxScope.Backdrop() } }
            // create wrapper
            contentWrappingTrait.WrapContent {
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Apply content holder trait
                    contentHolderTrait.run {
                        CreateContentHolder(
                            pages = stepContainer.steps.map { { it.content.Compose() } },
                            pageIndex = position
                        )
                    }

                    // Compose all container traits on top of the content Column
                    containerTraits.forEach { it.run { Overlay() } }
                }
            }
        }
    }

    override fun finish() {
        super.finish()
        viewModel.onEndExperience()
        // remove exit animation from this activity
        overridePendingTransition(0, 0)
    }
}
