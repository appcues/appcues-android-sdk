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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.core.os.bundleOf
import com.appcues.di.AppcuesKoinContext
import com.appcues.trait.ContentHolderTrait.ContainerPages
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
        CompositionLocalProvider(
            LocalAppcuesActionDelegate provides AppcuesActions { viewModel.onAction(it) },
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                // collect all UIState
                viewModel.uiState.collectAsState().let { state ->
                    // update last rendering state based on new state
                    rememberLastRenderingState(state).run {
                        // render last known rendering state
                        value?.let { ComposeLastRenderingState(it) }
                    }

                    // will run when transition from visible to gone is completed
                    LaunchOnHideAnimationCompleted {
                        // if state is dismissing then finish activity
                        if (state.value is Dismissing) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun rememberLastRenderingState(state: State<UIState>) = remember { mutableStateOf<Rendering?>(null) }
        .apply {
            value = state.value.let { uiState ->
                if (uiState is Rendering) {
                    // if UIState is rendering then we set new value and show content
                    isContentVisible.targetState = true
                    uiState
                } else {
                    // else we keep the same value and hide content to trigger dismissing animation
                    isContentVisible.targetState = false
                    value
                }
            }
        }

    @Composable
    private fun BoxScope.ComposeLastRenderingState(state: Rendering) {
        with(state.stepContainer) {
            // apply backdrop traits
            backdropTraits.forEach {
                with(it) {
                    Backdrop()
                }
            }
            // create wrapper
            contentWrappingTrait.WrapContent {
                Box(
                    contentAlignment = Alignment.TopCenter
                ) {
                    // Apply content holder trait
                    with(contentHolderTrait) {
                        // create object that will passed down to CreateContentHolder
                        ContainerPages(
                            pageCount = steps.size,
                            currentPage = state.position,
                            composePage = { index ->
                                with(steps[index]) {
                                    CompositionLocalProvider(LocalAppcuesActions provides actions) {
                                        // used to get the padding values from step decorating trait and apply to the Column
                                        val rememberPadding = rememberStepDecoratingPadding(LocalDensity.current)
                                        // Our page content
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .verticalScroll(rememberScrollState())
                                                .padding(paddingValues = rememberPadding.value.toPaddingValues())
                                        ) {
                                            content.Compose()
                                        }

                                        // apply step decorating traits
                                        traits.forEach { with(it) { Overlay(rememberPadding.value) } }
                                    }
                                }
                            }
                        ).also {
                            // create content holder
                            CreateContentHolder(it)
                            // sync pagination data in case content holder didn't update it
                            it.syncPaginationData()
                        }
                    }
                    // Compose all container traits on top of the content Column
                    containerTraits.forEach {
                        with(it) {
                            Overlay()
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    override fun finish() {
        super.finish()
        // remove exit animation from this activity
        overridePendingTransition(0, 0)

        viewModel.onFinish()
    }
}
