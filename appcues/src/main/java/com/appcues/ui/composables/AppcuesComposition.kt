package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalDensity
import com.appcues.logging.Logcues
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.trait.StepDecoratingPadding
import com.appcues.ui.AppcuesViewModel
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.ShakeGestureListener
import com.appcues.ui.theme.AppcuesTheme
import com.appcues.ui.utils.margin

@Composable
internal fun AppcuesComposition(
    viewModel: AppcuesViewModel,
    shakeGestureListener: ShakeGestureListener,
    logcues: Logcues,
    applySystemMargins: Boolean,
    onCompositionDismissed: () -> Unit,
) {
    // ensure to change some colors to match appropriate design for custom primitive blocks
    AppcuesTheme {
        // define composition local provided dependencies
        CompositionLocalProvider(
            LocalViewModel provides viewModel,
            LocalShakeGestureListener provides shakeGestureListener,
            LocalLogcues provides logcues,
            LocalAppcuesActionDelegate provides AppcuesActionsDelegate(viewModel),
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) },
        ) {
            MainSurface(
                applySystemMargins = rememberUpdatedState(applySystemMargins),
                onCompositionDismissed = onCompositionDismissed
            )
        }
    }
}

@Composable
private fun MainSurface(
    applySystemMargins: State<Boolean>,
    onCompositionDismissed: () -> Unit,
) {
    Box(
        modifier = Modifier.applySystemMargins(applySystemMargins),
        contentAlignment = Alignment.Center,
    ) {
        val viewModel = LocalViewModel.current
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
                    onCompositionDismissed()
                }
            }
        }
    }
}

private fun Modifier.applySystemMargins(shouldApply: State<Boolean>): Modifier = composed {
    then(
        if (shouldApply.value) {
            Modifier.margin(rememberSystemMarginsState().value)
        } else Modifier
    )
}

@Composable
private fun BoxScope.ComposeLastRenderingState(state: Rendering) {
    val shakeGestureListener = LocalShakeGestureListener.current
    val viewModel = LocalViewModel.current
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
