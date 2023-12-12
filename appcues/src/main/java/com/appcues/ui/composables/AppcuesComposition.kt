package com.appcues.ui.composables

import android.webkit.WebChromeClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.testTag
import coil.ImageLoader
import com.appcues.data.model.StepContainer
import com.appcues.logging.Logcues
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.ui.presentation.AppcuesViewModel
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.theme.AppcuesExperienceTheme

@Composable
internal fun AppcuesComposition(
    viewModel: AppcuesViewModel,
    imageLoader: ImageLoader,
    logcues: Logcues,
    chromeClient: WebChromeClient,
) {
    // ensure to change some colors to match appropriate design for custom primitive blocks
    AppcuesExperienceTheme {
        // define composition local provided dependencies
        CompositionLocalProvider(
            LocalImageLoader provides imageLoader,
            LocalViewModel provides viewModel,
            LocalLogcues provides logcues,
            LocalChromeClient provides chromeClient,
            LocalAppcuesActionDelegate provides DefaultAppcuesActionsDelegate(viewModel),
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) },
            LocalExperienceCompositionState provides ExperienceCompositionState()
        ) {
            MainSurface()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun MainSurface() {
    Box(
        modifier = Modifier.pointerInteropFilter { false },
        contentAlignment = Alignment.Center
    ) {
        val viewModel = LocalViewModel.current
        // collect all UIState
        viewModel.uiState.collectAsState().let { state ->
            // update last rendering state based on new state
            rememberLastRenderingState(state).run {
                // render last known rendering state
                value?.let { ComposeContainer(it.stepContainer, it.position, it.metadata) }
            }

            // will run when transition from visible to gone is completed
            LaunchOnHideAnimationCompleted {
                (state.value as? Dismissing)?.run { viewModel.onDismissed(awaitDismissEffect) }
            }

            val experienceState = LocalExperienceCompositionState.current
            LaunchedEffect(state.value) {
                state.value.let { uiState ->
                    if (uiState is Rendering) {
                        // trigger showing of content
                        experienceState.isContentVisible.targetState = true
                        experienceState.isBackdropVisible.targetState = true
                    } else {
                        // trigger dismissal of content
                        experienceState.isContentVisible.targetState = false
                        experienceState.isBackdropVisible.targetState = false
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.ComposeContainer(stepContainer: StepContainer, stepIndex: Int, metadata: Map<String, Any?>) {
    with(stepContainer) {
        val backdropDecoratingTraits = remember(stepIndex) { mutableStateOf(steps[stepIndex].backdropDecoratingTraits) }
        val containerDecoratingTraits = remember(stepIndex) { mutableStateOf(steps[stepIndex].containerDecoratingTraits) }

        val stepMetadata = remember { mutableStateOf<AppcuesStepMetadata?>(null) }
        val previousStepMetaData = remember { mutableStateOf(AppcuesStepMetadata()) }

        stepMetadata.value = AppcuesStepMetadata(previous = previousStepMetaData.value.current, current = metadata).also {
            previousStepMetaData.value = it
        }

        stepMetadata.value?.let { metadata ->
            CompositionLocalProvider(LocalAppcuesStepMetadata provides metadata) {
                // apply backdrop traits
                ApplyBackgroundDecoratingTraits(backdropDecoratingTraits.value)

                // create wrapper
                contentWrappingTrait.WrapContent { modifier, containerPadding, safeAreaInsets, hasVerticalScroll ->
                    Box(contentAlignment = Alignment.TopCenter) {
                        ApplyUnderlayContainerTraits(containerDecoratingTraits.value, containerPadding, safeAreaInsets)

                        // Apply content holder trait
                        with(contentHolderTrait) {
                            // create object that will passed down to CreateContentHolder
                            ContainerPages(
                                pageCount = steps.size,
                                currentPage = stepIndex,
                                composePage = {
                                    steps[it]
                                        .ComposeStep(
                                            modifier = modifier.testTag("page_$it"),
                                            containerPadding = containerPadding,
                                            safeAreaInsets = safeAreaInsets,
                                            parent = this@Box,
                                            hasVerticalScroll = hasVerticalScroll,
                                        )
                                }
                            ).also {
                                // create content holder
                                CreateContentHolder(it)
                                // sync pagination data in case content holder didn't update it
                                it.SyncPaginationData()
                            }
                        }

                        ApplyOverlayContainerTraits(containerDecoratingTraits.value, containerPadding, safeAreaInsets)
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoxScope.ApplyUnderlayContainerTraits(
    list: List<ContainerDecoratingTrait>,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
) {
    list
        .filter { it.containerComposeOrder == ContainerDecoratingType.UNDERLAY }
        .forEach { it.run { DecorateContainer(containerPadding, safeAreaInsets) } }
}

@Composable
private fun BoxScope.ApplyBackgroundDecoratingTraits(list: List<BackdropDecoratingTrait>) {
    // get last trait if its not null compose it and drop last calling it again recursively
    val item = list.lastOrNull()
    if (item != null) {
        with(item) { BackdropDecorate { ApplyBackgroundDecoratingTraits(list.dropLast(1)) } }
    }
}

@Composable
internal fun BoxScope.ApplyOverlayContainerTraits(
    list: List<ContainerDecoratingTrait>,
    containerPadding: PaddingValues,
    safeAreaInsets: PaddingValues,
) {
    list
        .filter { it.containerComposeOrder == ContainerDecoratingType.OVERLAY }
        .forEach { it.run { DecorateContainer(containerPadding, safeAreaInsets) } }
}
