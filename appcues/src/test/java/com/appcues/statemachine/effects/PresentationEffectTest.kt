package com.appcues.statemachine.effects

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.ExperienceTrigger.ShowCall
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Error
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.MetadataSettingTrait
import com.appcues.trait.PresentingTrait
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class PresentationEffectTest {

    private val experience = mockk<Experience>(relaxed = true)

    private val actionProcessor = mockk<ActionProcessor>(relaxed = true)

    @Test
    fun `launch SHOULD process navigation actions WHEN stepContainer is not 0`() = runTest {
        // GIVEN
        val navigationActions = listOf<ExperienceAction>(mockk(), mockk())
        every { experience.getNavigationActions(2) } returns navigationActions
        every { experience.trigger } returns Qualification("screen_view")
        val effect = PresentationEffect(experience, 0, 2, false)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        coVerify { actionProcessor.process(navigationActions) }
    }

    @Test
    fun `launch SHOULD process navigation actions WHEN experience is not Qualification`() = runTest {
        // GIVEN
        val navigationActions = listOf<ExperienceAction>(mockk(), mockk())
        every { experience.getNavigationActions(0) } returns navigationActions
        every { experience.trigger } returns ShowCall
        val effect = PresentationEffect(experience, 0, 0, false)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        coVerify { actionProcessor.process(navigationActions) }
    }

    @Test
    fun `launch SHOULD not process navigation actions WHEN experience is Qualification`() = runTest {
        // GIVEN
        val navigationActions = listOf<ExperienceAction>(mockk(), mockk())
        every { experience.getNavigationActions(0) } returns navigationActions
        every { experience.trigger } returns Qualification("testing")
        val effect = PresentationEffect(experience, 0, 0, false)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        coVerify { actionProcessor wasNot Called }
    }

    @Test
    fun `launch SHOULD call presentingTrait present if shouldPresent is true`() = runTest {
        // GIVEN
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf()
        val effect = PresentationEffect(experience, flatStepIndex, 0, true)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        verify { presentingTrait.present() }
    }

    @Test
    fun `launch SHOULD not call presentingTrait present if shouldPresent is false`() = runTest {
        // GIVEN
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf()
        val effect = PresentationEffect(experience, flatStepIndex, 0, false)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        verify { presentingTrait wasNot Called }
    }

    @Test
    fun `launch SHOULD call produceMetadata for each trait`() = runTest {
        // GIVEN
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val metadataSettingTrait1 = mockk<MetadataSettingTrait>(relaxed = true)
        val metadataSettingTrait2 = mockk<MetadataSettingTrait>(relaxed = true)
        val metadataSettingTrait3 = mockk<MetadataSettingTrait>(relaxed = true)
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf(
            metadataSettingTrait1,
            metadataSettingTrait2,
            metadataSettingTrait3
        )
        val effect = PresentationEffect(experience, flatStepIndex, 0, false)
        // WHEN
        effect.launch(actionProcessor)
        // THEN
        verifySequence {
            metadataSettingTrait1.produceMetadata()
            metadataSettingTrait2.produceMetadata()
            metadataSettingTrait3.produceMetadata()
        }
    }

    @Test
    fun `launch SHOULD return RenderStep WITH proper metadata`() = runTest {
        // GIVEN
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val metadataSettingTrait1 = mockk<MetadataSettingTrait>(relaxed = true) {
            every { produceMetadata() } returns mapOf("a" to "valueOf-a")
        }
        val metadataSettingTrait2 = mockk<MetadataSettingTrait>(relaxed = true) {
            every { produceMetadata() } returns mapOf("b" to "valueOf-b")
        }
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf(
            metadataSettingTrait1,
            metadataSettingTrait2,
        )
        val effect = PresentationEffect(experience, flatStepIndex, 0, false)
        // WHEN
        val result = effect.launch(actionProcessor)
        // THEN
        assertThat(result).isInstanceOf(RenderStep::class.java)
        with(result as RenderStep) {
            assertThat(metadata).hasSize(2)
            assertThat(metadata).containsEntry("a", "valueOf-a")
            assertThat(metadata).containsEntry("b", "valueOf-b")
        }
    }

    @Test
    fun `launch SHOULD return ReportError AND call remove() WHEN produceMetadata throws`() = runTest {
        // GIVEN
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val metadataSettingTrait1 = mockk<MetadataSettingTrait>(relaxed = true) {
            every { produceMetadata() } throws AppcuesTraitException("produceMetadata error")
        }
        val metadataSettingTrait2 = mockk<MetadataSettingTrait>(relaxed = true) {
            every { produceMetadata() } returns mapOf("b" to "valueOf-b")
        }
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf(
            metadataSettingTrait1,
            metadataSettingTrait2,
        )
        val effect = PresentationEffect(experience, flatStepIndex, 0, false)
        // WHEN
        val result = effect.launch(actionProcessor)
        // THEN
        coVerifySequence { presentingTrait.remove() }
        assertThat(result).isInstanceOf(ReportError::class.java)
        with(result as ReportError) {
            assertThat(error).isInstanceOf(Error.StepError::class.java)
            with(error as Error.StepError) {
                assertThat(experience).isEqualTo(this@PresentationEffectTest.experience)
                assertThat(stepIndex).isEqualTo(flatStepIndex)
                assertThat(message).isEqualTo("produceMetadata error")
            }
        }
    }

    @Test
    fun `launch SHOULD retry produceMetadata 3 times after 4 seconds WHEN shouldPresent AND produceMetadata throws`() = runTest {
        // GIVEN
        var remainingThrows = 3
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val metadataSettingTrait = mockk<MetadataSettingTrait>(relaxed = true) {
            every { produceMetadata() } answers {
                remainingThrows--
                // will throw with retry of 4 seconds 3 times
                throw AppcuesTraitException("produceMetadata error", retryMilliseconds = if (remainingThrows > 0) 4000 else null)
            }
        }
        val flatStepIndex = 0
        every { experience.getPresentingTrait(flatStepIndex) } returns presentingTrait
        every { experience.getMetadataSettingTraits(flatStepIndex) } returns listOf(metadataSettingTrait)
        val effect = PresentationEffect(experience, flatStepIndex, 0, true)
        // WHEN
        val result = effect.launch(actionProcessor)
        // THEN
        coVerifySequence {
            metadataSettingTrait.produceMetadata()
            advanceTimeBy(4000)
            metadataSettingTrait.produceMetadata()
            advanceTimeBy(4000)
            metadataSettingTrait.produceMetadata()
        }
        // after retrying returns ReportError
        assertThat(result).isInstanceOf(ReportError::class.java)
        with(result as ReportError) {
            assertThat(error).isInstanceOf(Error.StepError::class.java)
            with(error as Error.StepError) {
                assertThat(experience).isEqualTo(this@PresentationEffectTest.experience)
                assertThat(stepIndex).isEqualTo(flatStepIndex)
                assertThat(message).isEqualTo("produceMetadata error")
            }
        }
    }
}
