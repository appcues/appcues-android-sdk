package com.appcues.mocks

import com.appcues.action.appcues.TrackEventAction
import com.appcues.data.model.Action
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePriority.LOW
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.ExperienceTrigger.ShowCall
import com.appcues.data.model.Experiment
import com.appcues.data.model.RenderContext
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.trait.PresentingTrait
import io.mockk.every
import io.mockk.mockk
import java.util.UUID

internal fun mockPresentingTrait(onPresent: (() -> Unit)? = null): PresentingTrait = mockk(relaxed = true) {
    every { this@mockk.present() } answers { onPresent?.invoke() }
}

// an experience with a group having 3 steps, then a single step group
internal fun mockExperience(onPresent: (() -> Unit)? = null) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience",
        type = "mobile",
        renderContext = RenderContext.Modal,
        stepContainers = mutableListOf<StepContainer>().apply {
            val presentingTrait1 = mockPresentingTrait(onPresent)

            add(
                StepContainer(
                    id = UUID.fromString("e062bd81-b736-44c4-abba-633dfff966aa"),
                    steps = listOf(
                        mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), presentingTrait1),
                        mockStep(UUID.fromString("945e9689-8707-4196-8aa5-7c00c479bdab"), presentingTrait1),
                        mockStep(UUID.fromString("933f975e-7d38-4285-847d-ae4166b97618"), presentingTrait1),
                    ),
                    contentHolderTrait = mockk(relaxed = true),
                    contentWrappingTrait = mockk(relaxed = true),
                    actions = emptyMap(),
                )
            )

            val presentingTrait2 = mockPresentingTrait(onPresent)
            add(
                StepContainer(
                    id = UUID.fromString("373578ad-6371-4aa9-8645-79bffa2bc1a9"),
                    steps = listOf(
                        mockStep(UUID.fromString("0f6cda9d-17f0-4c0d-b8e7-e4fb94a128d9"), presentingTrait2),
                    ),
                    contentHolderTrait = mockk(relaxed = true),
                    contentWrappingTrait = mockk(relaxed = true),
                    actions = emptyMap(),
                )
            )
        },
        published = true,
        priority = LOW,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = arrayListOf(TrackEventAction(hashMapOf(), analyticsTracker = mockk(relaxed = true))),
        trigger = ShowCall,
        localeId = null,
        localeName = null,
        workflowId = null,
        workflowTaskId = null,
    )

internal fun mockStep(id: UUID, presentingTrait: PresentingTrait) =
    Step(
        id = id,
        content = TextPrimitive(
            id = UUID.fromString("df3bbe3e-8bdb-417a-b644-5e23862786b2"),
            spans = listOf()
        ),
        stepDecoratingTraits = listOf(),
        actions = mapOf(),
        type = "modal",
        presentingTrait = presentingTrait,
        backdropDecoratingTraits = mockk(relaxed = true),
        containerDecoratingTraits = mockk(relaxed = true),
        metadataSettingTraits = mockk(relaxed = true),
    )

internal fun mockExperienceExperiment(experiment: Experiment) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience with Experiment",
        type = "mobile",
        renderContext = RenderContext.Modal,
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), mockPresentingTrait()),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = experiment,
        completionActions = emptyList(),
        trigger = ShowCall,
        localeId = null,
        localeName = null,
        workflowId = null,
        workflowTaskId = null,
    )

internal fun mockEmbedExperience(frameId: String, onPresent: (() -> Unit)? = null) =
    Experience(
        id = UUID.fromString("15d713ce-5a8f-42d0-9f2c-85b6f1327f45"),
        name = "Mock Embed Experience",
        type = "mobile",
        renderContext = RenderContext.Embed(frameId),
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), mockPresentingTrait(onPresent)),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = emptyList(),
        trigger = ExperienceTrigger.Qualification("screen_view"),
        localeId = null,
        localeName = null,
        workflowId = null,
        workflowTaskId = null,
    )

// An experience with two step containers, each with one step. The given list of actions are applied to both
// step containers, to test pre-step navigation actions. The given PresentingTrait is applied to both
// step containers, to verify the order of actions prior to presentation.
internal fun mockExperienceNavigateActions(actions: List<Action>, presentingTrait: PresentingTrait, trigger: ExperienceTrigger) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience with Experiment",
        type = "mobile",
        renderContext = RenderContext.Modal,
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), presentingTrait),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = mapOf(UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694") to actions),
            ),
            StepContainer(
                id = UUID.fromString("71614c07-3f37-4f04-a853-f55424160321"),
                steps = listOf(
                    mockStep(UUID.fromString("c4b1c500-6939-4bb1-95ff-451bd6472cda"), presentingTrait),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = mapOf(UUID.fromString("71614c07-3f37-4f04-a853-f55424160321") to actions),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = emptyList(),
        trigger = trigger,
        localeId = null,
        localeName = null,
        workflowId = null,
        workflowTaskId = null,
    )

internal fun mockLocalizedExperience(localeName: String, localeId: String) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Localized Experience",
        type = "mobile",
        renderContext = RenderContext.Modal,
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("e062bd81-b736-44c4-abba-633dfff966aa"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), mockPresentingTrait()),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = LOW,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = arrayListOf(TrackEventAction(hashMapOf(), analyticsTracker = mockk(relaxed = true))),
        trigger = ShowCall,
        localeId = localeId,
        localeName = localeName,
        workflowId = null,
        workflowTaskId = null,
    )

internal fun mockWorkflowExperience(workflowId: UUID, workflowTaskId: UUID) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Localized Experience",
        type = "mobile",
        renderContext = RenderContext.Modal,
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("e062bd81-b736-44c4-abba-633dfff966aa"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902"), mockPresentingTrait()),
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = LOW,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = arrayListOf(TrackEventAction(hashMapOf(), analyticsTracker = mockk(relaxed = true))),
        trigger = ShowCall,
        localeId = null,
        localeName = null,
        workflowId = workflowId,
        workflowTaskId = workflowTaskId,
    )
