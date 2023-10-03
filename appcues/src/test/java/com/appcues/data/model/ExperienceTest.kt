package com.appcues.data.model

import com.appcues.data.model.Action.Trigger
import com.appcues.data.model.Action.Trigger.NAVIGATE
import com.appcues.data.model.Action.Trigger.TAP
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.PresentingTrait
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import org.junit.Test
import java.util.Date
import java.util.UUID

internal class ExperienceTest {

    @Test
    fun `getNavigationSteps SHOULD return 2 items`() {
        // GIVEN
        val id = UUID.randomUUID()
        val action1 = getAction(NAVIGATE)
        val action2 = getAction(NAVIGATE)
        val stepContainer = getStepContainer(id, actions = mapOf(id to listOf(action1, action2)))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val actions = experience.getNavigationActions(0)
        // THEN
        assertThat(actions).hasSize(2)
        assertThat(actions[0]).isEqualTo(action1.experienceAction)
        assertThat(actions[1]).isEqualTo(action2.experienceAction)
    }

    @Test
    fun `getNavigationSteps SHOULD return 0 items WHEN stepContainer is invalid`() {
        // GIVEN
        val id = UUID.randomUUID()
        val action1 = getAction(NAVIGATE)
        val action2 = getAction(NAVIGATE)
        val stepContainer = getStepContainer(id, actions = mapOf(id to listOf(action1, action2)))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val actions = experience.getNavigationActions(2)
        // THEN
        assertThat(actions).hasSize(0)
    }

    @Test
    fun `getNavigationSteps SHOULD return 0 items WHEN action id are different than stepContainer id`() {
        // GIVEN
        val action1 = getAction(NAVIGATE)
        val action2 = getAction(NAVIGATE)
        val stepContainer = getStepContainer(UUID.randomUUID(), actions = mapOf(UUID.randomUUID() to listOf(action1, action2)))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val actions = experience.getNavigationActions(0)
        // THEN
        assertThat(actions).hasSize(0)
    }

    @Test
    fun `getNavigationSteps SHOULD return 1 items WHEN only 1 is NAVIGATE`() {
        // GIVEN
        val id = UUID.randomUUID()
        val action1 = getAction(NAVIGATE)
        val action2 = getAction(TAP)
        val stepContainer = getStepContainer(id, actions = mapOf(id to listOf(action1, action2)))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val actions = experience.getNavigationActions(0)
        // THEN
        assertThat(actions).hasSize(1)
        assertThat(actions[0]).isEqualTo(action1.experienceAction)
    }

    @Test
    fun `areStepsFromDifferentGroup SHOULD return true WHEN step 0 and step 1 are from different group`() {
        // GIVEN
        val id = UUID.randomUUID()
        val stepContainer1 = getStepContainer(steps = arrayListOf(mockk()))
        val stepContainer2 = getStepContainer(steps = arrayListOf(mockk()))
        val experience = getExperience(listOf(stepContainer1, stepContainer2))
        // WHEN
        val result = experience.areStepsFromDifferentGroup(0, 1)
        // THEN
        assertThat(result).isTrue()
    }

    @Test
    fun `areStepsFromDifferentGroup SHOULD return false WHEN step 0 and step 1 are from the same group`() {
        // GIVEN
        val id = UUID.randomUUID()
        val stepContainer1 = getStepContainer(steps = arrayListOf(mockk(), mockk()))
        val stepContainer2 = getStepContainer(steps = arrayListOf())
        val experience = getExperience(listOf(stepContainer1, stepContainer2))
        // WHEN
        val result = experience.areStepsFromDifferentGroup(0, 1)
        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `getPresentingTrait SHOULD return present trait from stepContainer`() {
        // GIVEN
        val trait1 = mockk<PresentingTrait>()
        val stepContainer = getStepContainer(presentingTrait = trait1, steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val result = experience.getPresentingTrait(0)
        // THEN
        assertThat(result).isEqualTo(trait1)
    }

    @Test
    fun `getPresentingTrait SHOULD return same trait for both step index`() {
        // GIVEN
        val trait1 = mockk<PresentingTrait>()
        val stepContainer = getStepContainer(presentingTrait = trait1, steps = listOf(mockk(), mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val result1 = experience.getPresentingTrait(0)
        val result2 = experience.getPresentingTrait(1)
        // THEN
        assertThat(result1).isEqualTo(trait1)
        assertThat(result2).isEqualTo(trait1)
    }

    @Test
    fun `getPresentingTrait SHOULD return different trait for both step index`() {
        // GIVEN
        val trait1 = mockk<PresentingTrait>()
        val stepContainer1 = getStepContainer(presentingTrait = trait1, steps = listOf(mockk()))
        val trait2 = mockk<PresentingTrait>()
        val stepContainer2 = getStepContainer(presentingTrait = trait2, steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer1, stepContainer2))
        // WHEN
        val result1 = experience.getPresentingTrait(0)
        val result2 = experience.getPresentingTrait(1)
        // THEN
        assertThat(result1).isEqualTo(trait1)
        assertThat(result2).isEqualTo(trait2)
    }

    @Test(expected = AppcuesTraitException::class)
    fun `getPresentingTrait SHOULD throw WHEN flatStepIndex is invalid`() {
        // GIVEN
        val trait1 = mockk<PresentingTrait>()
        val stepContainer = getStepContainer(presentingTrait = trait1, steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        experience.getPresentingTrait(4)
    }

    @Test
    fun `isValidStepIndex SHOULD return true AND validate contract`() {
        // GIVEN
        val stepContainer = getStepContainer(steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer))
        @Suppress("RedundantNullableReturnType") val valStepIndex: Int? = 0
        // WHEN
        if (experience.isValidStepIndex(valStepIndex)) {
            assertIndexNotNull(0, valStepIndex)
        } else {
            assert(false) { "stepIndex is not valid" }
        }
    }

    @Test
    fun `isValidStepIndex SHOULD return false WHEN stepIndex is null`() {
        // GIVEN
        val stepContainer = getStepContainer(steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val result = experience.isValidStepIndex(null)
        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidStepIndex SHOULD return false WHEN stepIndex less than 0`() {
        // GIVEN
        val stepContainer = getStepContainer(steps = listOf(mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val result = experience.isValidStepIndex(-1)
        // THEN
        assertThat(result).isFalse()
    }

    @Test
    fun `isValidStepIndex SHOULD return false WHEN stepIndex greater than flatSteps count`() {
        // GIVEN
        val stepContainer = getStepContainer(steps = listOf(mockk(), mockk(), mockk()))
        val experience = getExperience(listOf(stepContainer))
        // WHEN
        val result = experience.isValidStepIndex(4)
        // THEN
        assertThat(result).isFalse()
    }

    @Suppress("SameParameterValue")
    private fun assertIndexNotNull(expected: Int, actual: Int) {
        assertThat(actual).isEqualTo(expected)
    }

    private fun getExperience(stepContainers: List<StepContainer> = listOf()): Experience {
        return Experience(
            id = UUID.randomUUID(),
            name = "Mock Experience",
            stepContainers = stepContainers,
            published = true,
            priority = NORMAL,
            type = "DRAFT",
            renderContext = RenderContext.Modal,
            publishedAt = Date().time,
            localeId = "US",
            localeName = "US",
            experiment = null,
            completionActions = arrayListOf(),
            trigger = ExperienceTrigger.Preview,
            requestId = null,
            error = null,
            renderErrorId = null,
        )
    }

    private fun getStepContainer(
        id: UUID = UUID.randomUUID(),
        steps: List<Step> = listOf(),
        actions: Map<UUID, List<Action>> = mapOf(),
        presentingTrait: PresentingTrait = mockk(),
    ): StepContainer {
        return StepContainer(
            id = id,
            steps = steps,
            actions = actions,
            presentingTrait = presentingTrait,
            contentHolderTrait = mockk(relaxed = true),
            contentWrappingTrait = mockk(relaxed = true),
        )
    }

    private fun getAction(on: Trigger): Action {
        return Action(on = on, experienceAction = mockk())
    }
}
