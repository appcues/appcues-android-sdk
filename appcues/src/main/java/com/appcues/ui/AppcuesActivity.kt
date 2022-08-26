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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.core.os.bundleOf
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.di.AppcuesKoinContext
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.trait.StepDecoratingPadding
import com.appcues.trait.StepDecoratingTrait.StepDecoratingType
import com.appcues.ui.AppcuesViewModel.UIState
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.primitive.Compose
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.utils.margin
import com.appcues.util.getNavigationBarHeight
import com.appcues.util.getStatusBarHeight
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

    private val shakeGestureListener: ShakeGestureListener by lazy { ShakeGestureListener(context = this) }

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

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        shakeGestureListener.start()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
        shakeGestureListener.stop()
    }

    @Composable
    private fun Composition() {
        CompositionLocalProvider(
            LocalAppcuesActionDelegate provides AppcuesActions { viewModel.onAction(it) },
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) },
            LocalLogcues provides scope.get(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .margin(rememberSystemMarginsState().value),
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
    private fun rememberSystemMarginsState(): State<PaddingValues> {
        val density = LocalDensity.current
        val topMargin = rememberUpdatedState(newValue = with(density) { LocalContext.current.getStatusBarHeight().toDp() })
        val bottomMargin = rememberUpdatedState(newValue = with(density) { LocalContext.current.getNavigationBarHeight().toDp() })
        // will calculate status bar height and navigation bar height and return it in PaddingValues
        // this is derived state to handle possible changes to values in top and bottom margin
        return derivedStateOf { PaddingValues(top = topMargin.value, bottom = bottomMargin.value) }
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
        LaunchedEffect(state.isPreview) {
            if (state.isPreview) {
                shakeGestureListener.addListener(true) {
                    viewModel.refreshPreview()
                }
            } else {
                shakeGestureListener.clearListener()
            }
        }

        with(state.stepContainer) {
            // apply backdrop traits
            backdropDecoratingTraits.forEach {
                with(it) { Backdrop() }
            }
            // create wrapper
            contentWrappingTrait.WrapContent { hasFixedHeight, contentPadding ->
                Box(contentAlignment = Alignment.TopCenter) {
                    ApplyUnderlayContainerTraits(this)

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
                                        val density = LocalDensity.current
                                        val stepDecoratingPadding = remember(this) { StepDecoratingPadding(density) }

                                        ApplyUnderlayStepTraits(this@Box, stepDecoratingPadding)

                                        ComposeStepContent(index, hasFixedHeight, contentPadding, stepDecoratingPadding)

                                        ApplyOverlayStepTraits(this@Box, stepDecoratingPadding)
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

                    ApplyOverlayContainerTraits(this)
                }
            }
        }
    }

    @Composable
    private fun StepContainer.ApplyUnderlayContainerTraits(boxScope: BoxScope) {
        containerDecoratingTraits
            .filter { it.containerComposeOrder == ContainerDecoratingType.UNDERLAY }
            .forEach { it.run { boxScope.DecorateContainer() } }
    }

    @Composable
    private fun StepContainer.ApplyOverlayContainerTraits(boxScope: BoxScope) {
        containerDecoratingTraits
            .filter { it.containerComposeOrder == ContainerDecoratingType.OVERLAY }
            .forEach { it.run { boxScope.DecorateContainer() } }
    }

    @Composable
    private fun Step.ComposeStepContent(
        index: Int,
        hasFixedHeight: Boolean,
        contentPadding: PaddingValues?,
        stepDecoratingPadding: StepDecoratingPadding
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                // if WrappingContent has a fixed height we fill height
                // else we will scale according to content
                .then(if (hasFixedHeight) Modifier.fillMaxHeight() else Modifier)
                .verticalScroll(rememberScrollState())
                // if we have contentPadding to apply from the WrapContent trait then we apply here
                .then(if (contentPadding != null) Modifier.padding(contentPadding) else Modifier)
                .padding(paddingValues = stepDecoratingPadding.paddingValues.value)
                .testTag("page_$index")
        ) {
            content.Compose()
        }
    }

    @Composable
    private fun Step.ApplyUnderlayStepTraits(boxScope: BoxScope, stepDecoratingPadding: StepDecoratingPadding) {
        stepDecoratingTraits
            .filter { it.stepComposeOrder == StepDecoratingType.UNDERLAY }
            .forEach { it.run { boxScope.DecorateStep(stepDecoratingPadding) } }
    }

    @Composable
    private fun Step.ApplyOverlayStepTraits(boxScope: BoxScope, stepDecoratingPadding: StepDecoratingPadding) {
        stepDecoratingTraits
            .filter { it.stepComposeOrder == StepDecoratingType.OVERLAY }
            .forEach { it.run { boxScope.DecorateStep(stepDecoratingPadding) } }
    }

    override fun onBackPressed() {
        // if we have a back pressed dispatcher enabled then we call it. its not a good practice
        // not call super onBackPressed but sometimes people do it, in that case the debugger wont
        // consume the back press properly and there is nothing we can do about it.
        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            super.onBackPressed()
        } else {
            viewModel.onBackPressed()
        }
    }

    override fun finish() {
        super.finish()
        // remove exit animation from this activity
        overridePendingTransition(0, 0)

        viewModel.onFinish()
    }
}
