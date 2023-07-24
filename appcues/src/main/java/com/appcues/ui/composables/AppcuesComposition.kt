package com.appcues.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import com.appcues.data.model.StepContainer
import com.appcues.logging.Logcues
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.trait.MetadataSettingTrait
import com.appcues.ui.AppcuesViewModel
import com.appcues.ui.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.ShakeGestureListener
import com.appcues.ui.theme.AppcuesTheme
import com.google.accompanist.web.AccompanistWebChromeClient
import kotlinx.coroutines.delay

@Composable
internal fun AppcuesComposition(
    viewModel: AppcuesViewModel,
    shakeGestureListener: ShakeGestureListener,
    logcues: Logcues,
    chromeClient: AccompanistWebChromeClient,
) {
    // ensure to change some colors to match appropriate design for custom primitive blocks
    AppcuesTheme {
        // define composition local provided dependencies
        CompositionLocalProvider(
            LocalViewModel provides viewModel,
            LocalShakeGestureListener provides shakeGestureListener,
            LocalLogcues provides logcues,
            LocalChromeClient provides chromeClient,
            LocalAppcuesActionDelegate provides DefaultAppcuesActionsDelegate(viewModel),
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) },
            LocalAppcuesExperienceVisibility provides AppcuesExperienceVisibility { viewModel.updateViewVisibility(it) },
            LocalTraitExceptionHandler provides AppcuesTraitExceptionHandler { viewModel.onTraitException(it) },
            LocalExperienceCompositionState provides ExperienceCompositionState()
        ) {
            MainSurface()
        }
    }
}

@Composable
private fun MainSurface() {
    Box(
        modifier = Modifier.consumeAllTouchEvents(),
        contentAlignment = Alignment.Center
    ) {
        val viewModel = LocalViewModel.current
        // collect all UIState
        viewModel.uiState.collectAsState().let { state ->
            // update last rendering state based on new state
            rememberLastRenderingState(state).run {
                // render last known rendering state
                value?.let { ComposeLastRenderingState(it) }
            }

            LaunchOnShowAnimationCompleted {
                viewModel.onPresentationComplete()
            }

            // will run when transition from visible to gone is completed
            LaunchOnHideAnimationCompleted {
                // if state is dismissing then finish activity
                if (state.value is Dismissing) {
                    viewModel.dismiss()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.consumeAllTouchEvents(): Modifier = then(
    Modifier
        .fillMaxSize()
        .pointerInteropFilter { false }
)

@Composable
private fun BoxScope.ComposeLastRenderingState(state: Rendering) {
    val shakeGestureListener = LocalShakeGestureListener.current
    val viewModel = LocalViewModel.current

    LaunchedEffect(state.isPreview) {
        if (state.isPreview) {
            shakeGestureListener.addListener(true) { viewModel.refreshPreview() }
        } else {
            shakeGestureListener.clearListener()
        }
    }

    ComposeContainer(state.stepContainer, state.position)
}

private suspend fun produceMetadata(
    metadataSettingTraits: List<MetadataSettingTrait>,
    traitExceptionHandler: AppcuesTraitExceptionHandler,
    experienceVisibility: AppcuesExperienceVisibility,
): HashMap<String, Any?>? {
    return try {
        hashMapOf<String, Any?>().apply { metadataSettingTraits.forEach { putAll(it.produceMetadata()) } }.also {
            experienceVisibility.setVisible(true)
        }
    } catch (ex: AppcuesTraitException) {
        if (ex.retryMilliseconds != null) {
            experienceVisibility.setVisible(false)
            delay(ex.retryMilliseconds.toLong())
            produceMetadata(metadataSettingTraits, traitExceptionHandler, experienceVisibility)
        } else {
            traitExceptionHandler.onTraitException(ex)
            null
        }
    }
}

@Composable
internal fun BoxScope.ComposeContainer(stepContainer: StepContainer, stepIndex: Int) {
    with(stepContainer) {
        val backdropDecoratingTraits = remember(stepIndex) { mutableStateOf(steps[stepIndex].backdropDecoratingTraits) }
        val containerDecoratingTraits = remember(stepIndex) { mutableStateOf(steps[stepIndex].containerDecoratingTraits) }
        val metadataSettingTraits = remember(stepIndex) { mutableStateOf(steps[stepIndex].metadataSettingTraits) }

        val traitExceptionHandler = LocalTraitExceptionHandler.current
        val experienceVisibility = LocalAppcuesExperienceVisibility.current
        val stepMetadata = remember { mutableStateOf<AppcuesStepMetadata?>(null) }
        val previousStepMetaData = remember { mutableStateOf(AppcuesStepMetadata()) }

        LaunchedEffect(metadataSettingTraits) {
            val actual = produceMetadata(metadataSettingTraits.value, traitExceptionHandler, experienceVisibility)
            if (actual != null) {
                stepMetadata.value = AppcuesStepMetadata(previous = previousStepMetaData.value.current, current = actual).also {
                    previousStepMetaData.value = it
                }
            }
        }

        stepMetadata.value?.let { metadata ->
            CompositionLocalProvider(LocalAppcuesStepMetadata provides metadata) {
                // apply backdrop traits
                ApplyBackgroundDecoratingTraits(backdropDecoratingTraits.value)

                // create wrapper
                contentWrappingTrait.WrapContent { modifier, containerPadding, safeAreaInsets ->
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
                                            parent = this@Box
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
