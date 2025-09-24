package com.appcues.ui.composables

import android.content.res.Configuration
import android.webkit.WebChromeClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import coil.ImageLoader
import com.appcues.data.model.StepContainer
import com.appcues.logging.Logcues
import com.appcues.monitor.AppcuesActivityMonitor
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.BackdropDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait
import com.appcues.trait.ContainerDecoratingTrait.ContainerDecoratingType
import com.appcues.trait.ContentHolderTrait.ContainerPages
import com.appcues.ui.presentation.AppcuesViewModel
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import com.appcues.ui.theme.AppcuesExperienceTheme
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM
import com.appcues.ui.utils.getParentView
import com.appcues.util.withDensity

@Composable
internal fun AppcuesComposition(
    viewModel: AppcuesViewModel,
    imageLoader: ImageLoader,
    logcues: Logcues,
    chromeClient: WebChromeClient,
    packageNames: List<String>
) {
    // ensure to change some colors to match appropriate design for custom primitive blocks
    AppcuesExperienceTheme {
        // define composition local provided dependencies
        CompositionLocalProvider(
            LocalImageLoader provides imageLoader,
            LocalViewModel provides viewModel,
            LocalLogcues provides logcues,
            LocalChromeClient provides chromeClient,
            LocalPackageNames provides packageNames,
            LocalAppcuesActionDelegate provides DefaultAppcuesActionsDelegate(viewModel),
            LocalAppcuesPaginationDelegate provides AppcuesPagination { viewModel.onPageChanged(it) },
            LocalExperienceCompositionState provides ExperienceCompositionState(),
            LocalAppcuesDismissalDelegate provides DefaultAppcuesDismissalDelegate(viewModel),
            LocalAppcuesTapForwardingDelegate provides DefaultAppcuesTapForwardingDelegate(viewModel),
            LocalAppcuesWindowInfo provides getWindowInfo()
        ) {
            MainSurface()
        }
    }
}

@Composable
private fun MainSurface() {
    Box(contentAlignment = Alignment.Center) {
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
                val isBlockingBackdrop = backdropDecoratingTraits.value.any { it.isBlocking }
                ApplyBackgroundDecoratingTraits(isBlockingBackdrop, backdropDecoratingTraits.value)

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
private fun BoxScope.ApplyBackgroundDecoratingTraits(isBlocking: Boolean, list: List<BackdropDecoratingTrait>) {
    // get last trait if its not null compose it and drop last calling it again recursively
    val item = list.lastOrNull()
    if (item != null) {
        with(item) { BackdropDecorate(isBlocking) { ApplyBackgroundDecoratingTraits(isBlocking, list.dropLast(1)) } }
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

@Composable
internal fun getWindowInfo(): AppcuesWindowInfo {
    val configuration = LocalConfiguration.current

    val view = AppcuesActivityMonitor.activity?.getParentView()
        ?: throw AppcuesTraitException("could not find root view")

    return view.withDensity {
        val insets = ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.systemBars())
            ?: Insets.NONE
        val insetsDp = insets.toDp()

        val size = Size(
            width = view.width.toDp().toFloat(),
            height = view.height.toDp().toFloat()
        )

        val safeInsets = WindowInsets(
            left = insetsDp.left.dp,
            top = insetsDp.top.dp,
            right = insetsDp.right.dp,
            bottom = insetsDp.bottom.dp
        )

        val safeRect = Rect(
            left = insetsDp.left.toFloat(),
            top = insetsDp.top.toFloat(),
            right = size.width - insetsDp.right.toFloat(),
            bottom = size.height - insetsDp.bottom.toFloat()
        )

        val screenWidthType = when {
            size.width < AppcuesWindowInfo.WIDTH_COMPACT -> COMPACT
            size.width < AppcuesWindowInfo.WIDTH_MEDIUM -> MEDIUM
            else -> EXPANDED
        }

        val screenHeightType = when {
            size.height < AppcuesWindowInfo.HEIGHT_COMPACT -> COMPACT
            size.height < AppcuesWindowInfo.HEIGHT_MEDIUM -> MEDIUM
            else -> EXPANDED
        }

        val orientation = when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> PORTRAIT
            else -> LANDSCAPE
        }

        val deviceType = when (orientation) {
            PORTRAIT -> when (screenWidthType) {
                COMPACT -> MOBILE
                MEDIUM -> TABLET
                EXPANDED -> TABLET
            }
            LANDSCAPE -> when (screenHeightType) {
                COMPACT -> MOBILE
                MEDIUM -> TABLET
                EXPANDED -> TABLET
            }
        }

        AppcuesWindowInfo(
            screenWidthType = screenWidthType,
            screenHeightType = screenHeightType,
            safeRect = safeRect,
            safeInsets = safeInsets,
            orientation = orientation,
            deviceType = deviceType,
        )
    }
}
