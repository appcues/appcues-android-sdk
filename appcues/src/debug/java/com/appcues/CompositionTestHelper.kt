package com.appcues

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.LayoutDirection.Ltr
import coil.ImageLoader
import com.appcues.action.ExperienceAction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.MoshiConfiguration
import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.mapper.step.primitives.mapPrimitive
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.remote.appcues.response.QualifyResponse
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse
import com.appcues.data.remote.appcues.response.step.primitive.PrimitiveResponse.StackPrimitiveResponse
import com.appcues.data.remote.appcues.response.trait.TraitResponse
import com.appcues.di.Bootstrap
import com.appcues.di.scope.get
import com.appcues.logging.Logcues
import com.appcues.ui.composables.AppcuesActionsDelegate
import com.appcues.ui.composables.AppcuesDismissalDelegate
import com.appcues.ui.composables.AppcuesTapForwardingDelegate
import com.appcues.ui.composables.ComposeContainer
import com.appcues.ui.composables.ExperienceCompositionState
import com.appcues.ui.composables.LocalAppcuesActionDelegate
import com.appcues.ui.composables.LocalAppcuesDismissalDelegate
import com.appcues.ui.composables.LocalAppcuesTapForwardingDelegate
import com.appcues.ui.composables.LocalAppcuesWindowInfo
import com.appcues.ui.composables.LocalExperienceCompositionState
import com.appcues.ui.composables.LocalExperienceStepFormStateDelegate
import com.appcues.ui.composables.LocalImageLoader
import com.appcues.ui.composables.LocalLogcues
import com.appcues.ui.primitive.Compose
import com.appcues.ui.theme.AppcuesExperienceTheme
import com.appcues.ui.utils.AppcuesWindowInfo
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.MOBILE
import com.appcues.ui.utils.AppcuesWindowInfo.DeviceType.TABLET
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.LANDSCAPE
import com.appcues.ui.utils.AppcuesWindowInfo.Orientation.PORTRAIT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.COMPACT
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.EXPANDED
import com.appcues.ui.utils.AppcuesWindowInfo.ScreenType.MEDIUM
import java.util.UUID

// This helper supports testing a single experience primitive (can have nested children),
// defined in the given json string.
@Composable
public fun ComposeContent(json: String, imageLoader: ImageLoader) {
    val response = MoshiConfiguration.moshi.adapter(PrimitiveResponse::class.java).fromJson(json)
    val primitive = response!!.mapPrimitive()

    AppcuesExperienceTheme {
        CompositionLocalProvider(
            LocalImageLoader provides imageLoader,
            LocalLogcues provides Logcues(),
            LocalExperienceStepFormStateDelegate provides ExperienceStepFormState(),
            LocalAppcuesActionDelegate provides FakeAppcuesActionDelegate(),
            LocalExperienceCompositionState provides ExperienceCompositionState(),
            LocalAppcuesWindowInfo provides testWindowInfo()
        ) {
            primitive.Compose()
        }
    }
}

public suspend fun composeTraits(
    context: Context,
    stepContentJson: List<String>?,
    traitJson: List<String>,
    imageLoader: ImageLoader,
    execute: (composable: @Composable () -> Unit) -> Unit,
) {
    // set up a scope for testing - for experience/trait mapping, trait registry, etc
    val scope = Bootstrap.createScope(context, AppcuesConfig("", ""))

    // the baseline experience is very simple, single step with empty content
    // read this in and then inject traits and content
    val experienceResponse = MoshiConfiguration.moshi.adapter(ExperienceResponse::class.java).fromJson(baseExperienceJSON)!!
    val traitResponses = traitJson.map {
        MoshiConfiguration.moshi.adapter(TraitResponse::class.java).fromJson(it)!!
    }

    // injecting content is optional, may only be testing a trait
    val stepContent = stepContentJson?.let { contentItems ->
        contentItems.map { contentItem ->
            MoshiConfiguration.moshi.adapter(PrimitiveResponse::class.java).fromJson(contentItem)!!
        }
    } ?: listOf()

    // update the experience to add the given traits at the experience level
    val updatedExperienceResponse = experienceResponse.copy(
        traits = traitResponses,
        steps = experienceResponse.steps.map { stepContainerResponse ->
            stepContainerResponse.copy(
                children = stepContainerResponse.children.map { stepResponse ->
                    stepResponse.copy(
                        // optionally inject the step content
                        content = StackPrimitiveResponse(
                            id = UUID.randomUUID(),
                            orientation = "vertical",
                            items = stepContent,
                        )
                    )
                }
            )
        }
    )

    // map the experience
    val experienceMapper: ExperienceMapper = scope.get()
    val experience = experienceMapper.map(updatedExperienceResponse, ExperienceTrigger.Preview)
    val container = experience.stepContainers[0]
    val metadataSettingTraits = container.steps[0].metadataSettingTraits
    val metadata = hashMapOf<String, Any?>().apply { metadataSettingTraits.forEach { putAll(it.produceMetadata()) } }

    execute {
        AppcuesExperienceTheme {
            CompositionLocalProvider(
                LocalImageLoader provides imageLoader,
                LocalLogcues provides Logcues(),
                LocalExperienceStepFormStateDelegate provides ExperienceStepFormState(),
                LocalAppcuesActionDelegate provides FakeAppcuesActionDelegate(),
                LocalExperienceCompositionState provides ExperienceCompositionState(
                    // disables animations
                    isContentVisible = MutableTransitionState(true),
                    isBackdropVisible = MutableTransitionState(true)
                ),
                LocalAppcuesDismissalDelegate provides FakeAppcuesDismissalDelegate(),
                LocalAppcuesTapForwardingDelegate provides FakeAppcuesTapForwardingDelegate(),
                LocalAppcuesWindowInfo provides testWindowInfo()
            ) {
                // render the step container on the desired step
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ComposeContainer(container, 0, metadata)
                }
            }
        }
    }
}

// This helper supports testing any experience JSON content, rendering the given group and step index.
public suspend fun composeExperience(
    context: Context,
    experienceJson: String,
    groupIndex: Int,
    stepIndex: Int,
    animated: Boolean,
    imageLoader: ImageLoader,
    execute: (composable: @Composable () -> Unit) -> Unit,
) {
    // set up a Koin scope for testing - for experience/trait mapping, trait registry, etc
    val scope = Bootstrap.createScope(context, AppcuesConfig("", ""))

    // this JSON is actually a qualify response, and we'll get the first experience out of it
    val qualifyResponse = MoshiConfiguration.moshi.adapter(QualifyResponse::class.java).fromJson(experienceJson)!!
    val experienceResponse = qualifyResponse.experiences[0] as ExperienceResponse

    // update the experience to strip out any actions, as they can rely on injected dependencies
    // that are not available using the test context
    val updatedExperienceResponse = experienceResponse.copy(
        steps = experienceResponse.steps.map { stepContainerResponse ->
            stepContainerResponse.copy(
                children = stepContainerResponse.children.map { stepResponse ->
                    stepResponse.copy(
                        actions = emptyMap()
                    )
                },
                actions = emptyMap()
            )
        },
    )

    // map the experience
    val experienceMapper: ExperienceMapper = scope.get()
    val experience = experienceMapper.map(updatedExperienceResponse, ExperienceTrigger.Preview)
    val container = experience.stepContainers[groupIndex]
    val metadataSettingTraits = container.steps[stepIndex].metadataSettingTraits
    val metadata = hashMapOf<String, Any?>().apply { metadataSettingTraits.forEach { putAll(it.produceMetadata()) } }

    execute {
        AppcuesExperienceTheme {
            CompositionLocalProvider(
                LocalImageLoader provides imageLoader,
                LocalLogcues provides Logcues(),
                LocalExperienceStepFormStateDelegate provides ExperienceStepFormState(),
                LocalAppcuesActionDelegate provides FakeAppcuesActionDelegate(),
                LocalExperienceCompositionState provides ExperienceCompositionState(
                    // disables animations
                    isContentVisible = MutableTransitionState(!animated),
                    isBackdropVisible = MutableTransitionState(!animated)
                ),
                LocalAppcuesDismissalDelegate provides FakeAppcuesDismissalDelegate(),
                LocalAppcuesTapForwardingDelegate provides FakeAppcuesTapForwardingDelegate(),
                LocalAppcuesWindowInfo provides testWindowInfo()
            ) {
                // render the step container on the desired step
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ComposeContainer(container, stepIndex, metadata)
                }

                if (animated) {
                    val compositionState = LocalExperienceCompositionState.current
                    LaunchedEffect(Unit) {
                        compositionState.isContentVisible.targetState = true
                        compositionState.isBackdropVisible.targetState = true
                    }
                }
            }
        }
    }
}

private var baseExperienceJSON =
    """
    {
        "id": "9f4baa80-8f6a-41b1-a7b9-979da5c175e2",
        "name": "Trait Testing",
        "type": "mobile",
        "traits": [],
        "steps": [
            {
                "id": "68c0d4b4-4909-4d4a-9ce4-7af8b04efab2",
                "parentId": "6c2b7488-309c-432f-b62e-9f8539b46c9d",
                "type": "modal",
                "contentType": "application/json",
                "content": {
                    "type": "stack",
                    "orientation": "vertical",
                    "id": "2cf7dbf7-c6be-4130-b642-85861f9c6b6a",
                    "style": {},
                    "items": []
                },
                "traits": [],
                "actions": {}
            }
        ]
    }
    """

private class FakeAppcuesActionDelegate : AppcuesActionsDelegate {

    override fun onActions(actions: List<ExperienceAction>, interactionType: InteractionType, viewDescription: String?) {
        // do nothing
    }
}

private class FakeAppcuesDismissalDelegate : AppcuesDismissalDelegate {
    override val canDismiss = false
    override fun requestDismissal() { }
}

private class FakeAppcuesTapForwardingDelegate: AppcuesTapForwardingDelegate {
    override fun onTap(offset: Offset) { }
}

@Composable
internal fun testWindowInfo(): AppcuesWindowInfo {
    val configuration = LocalConfiguration.current

    // For tests, use the LocalConfiguration.current to derive the available content size and insets.
    // This supports a snapshot testing environment where there is not a container/overlay view in use.
    val safeInsets = WindowInsets.safeDrawing
    val insetsDp = safeInsets.asPaddingValues()

    val size = Size(
        width = configuration.screenWidthDp.toFloat(),
        height = configuration.screenHeightDp.toFloat()
    )

    val safeRect = Rect(
        left = insetsDp.calculateLeftPadding(Ltr).value,
        top = insetsDp.calculateTopPadding().value,
        right = size.width - insetsDp.calculateRightPadding(Ltr).value,
        bottom = size.height - insetsDp.calculateBottomPadding().value
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

    return AppcuesWindowInfo(
        screenWidthType = screenWidthType,
        screenHeightType = screenHeightType,
        safeRect = safeRect,
        safeInsets = safeInsets,
        orientation = orientation,
        deviceType = deviceType,
    )
}
