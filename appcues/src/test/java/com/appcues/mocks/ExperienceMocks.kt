package com.appcues.mocks

import com.appcues.action.appcues.LaunchExperienceAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.data.model.Action
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.Experiment
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
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("e062bd81-b736-44c4-abba-633dfff966aa"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902")),
                    mockStep(UUID.fromString("945e9689-8707-4196-8aa5-7c00c479bdab")),
                    mockStep(UUID.fromString("933f975e-7d38-4285-847d-ae4166b97618")),
                ),
                presentingTrait = mockPresentingTrait(onPresent),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            ),
            StepContainer(
                id = UUID.fromString("373578ad-6371-4aa9-8645-79bffa2bc1a9"),
                steps = listOf(
                    mockStep(UUID.fromString("0f6cda9d-17f0-4c0d-b8e7-e4fb94a128d9")),
                ),
                presentingTrait = mockPresentingTrait(onPresent),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = arrayListOf(LaunchExperienceAction("1234"), TrackEventAction(hashMapOf()))
    )

internal fun mockStep(id: UUID) =
    Step(
        id = id,
        content = TextPrimitive(
            id = UUID.fromString("df3bbe3e-8bdb-417a-b644-5e23862786b2"),
            text = ""
        ),
        stepDecoratingTraits = listOf(),
        actions = mapOf(),
        type = "modal",
        backdropDecoratingTraits = mockk(relaxed = true),
        containerDecoratingTraits = mockk(relaxed = true),
        metadataSettingTraits = mockk(relaxed = true),
    )

internal fun mockExperienceExperiment(experiment: Experiment) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience with Experiment",
        type = "mobile",
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902")),
                ),
                presentingTrait = mockPresentingTrait(),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = experiment,
        completionActions = emptyList()
    )

// An experience with two step containers, each with one step. The given list of actions are applied to the
// second step container, to test pre-step navigation actions. The given PresentingTrait is applied to both
// step containers, to verify the order of actions prior to presentation.
internal fun mockExperienceNavigateActions(actions: List<Action>, presentingTrait: PresentingTrait) =
    Experience(
        id = UUID.fromString("d84c9d01-aa27-4cbb-b832-ee03720e04fc"),
        name = "Mock Experience with Experiment",
        type = "mobile",
        stepContainers = listOf(
            StepContainer(
                id = UUID.fromString("60b49c12-c49b-47ac-8ed3-ba4e9a55e694"),
                steps = listOf(
                    mockStep(UUID.fromString("01d8a05a-3a55-4ecc-872d-d140cd628902")),
                ),
                presentingTrait = presentingTrait,
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            ),
            StepContainer(
                id = UUID.fromString("71614c07-3f37-4f04-a853-f55424160321"),
                steps = listOf(
                    mockStep(UUID.fromString("c4b1c500-6939-4bb1-95ff-451bd6472cda")),
                ),
                presentingTrait = presentingTrait,
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = mapOf(UUID.fromString("71614c07-3f37-4f04-a853-f55424160321") to actions),
            )
        ),
        published = true,
        priority = NORMAL,
        publishedAt = 1652895835000,
        experiment = null,
        completionActions = emptyList()
    )
