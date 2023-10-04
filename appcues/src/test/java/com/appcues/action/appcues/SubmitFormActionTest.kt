package com.appcues.action.appcues

import com.appcues.Appcues
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.FORM_SUBMITTED
import com.appcues.analytics.formattedAsProfileUpdate
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive
import com.appcues.data.model.ExperiencePrimitive.OptionSelectPrimitive.OptionItem
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.data.model.styling.ComponentSelectMode.MULTIPLE
import com.appcues.statemachine.State
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.Date
import java.util.UUID

internal class SubmitFormActionTest {

    @Test
    fun `submit-form SHOULD have expected type name`() {
        assertThat(SubmitFormAction.TYPE).isEqualTo("@appcues/submit-form")
    }

    @Test
    fun `submit-form SHOULD call analyticsTracker WHEN form is valid`() = runTest {
        // GIVEN
        val optionSelect = optionSelect(5, 1)
        val textInput = textInput()
        val formState = formState(optionSelect, textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val action = SubmitFormAction(emptyMap(), RenderContext.Modal, experienceRenderer, analyticsTracker)
        val analyticEvent = StepInteraction(experience, 0, FORM_SUBMITTED)

        // WHEN
        formState.setValue(textInput, "text")
        formState.setValue(optionSelect, "0")
        action.execute()

        // THEN
        assertThat(formState.isFormComplete).isTrue()
        verify { analyticsTracker.identify(formState.formattedAsProfileUpdate(), interactive = false) }
        verify { analyticsTracker.track(analyticEvent.name, analyticEvent.properties, interactive = false, isInternal = true) }
    }

    @Test
    fun `submit-form SHOULD track custom profile attribute WHEN attributeName is set`() = runTest {
        // GIVEN
        val textInput = TextInputPrimitive(
            id = UUID.randomUUID(),
            label = TextPrimitive(id = UUID.randomUUID(), spans = listOf(TextSpanPrimitive("label"))),
            required = true,
            attributeName = "myCustomAttribute"
        )
        val formState = formState(textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val action = SubmitFormAction(emptyMap(), RenderContext.Modal, experienceRenderer, analyticsTracker)

        // WHEN
        formState.setValue(textInput, "text")
        action.execute()

        // THEN
        verify {
            analyticsTracker.identify(mapOf("_appcuesForm_label" to "text", "myCustomAttribute" to "text"), interactive = false)
        }
    }

    // seems an odd test here, validating that it submits on invalid input - but this is confirming the
    // analytics behavior on execute - which is independent of the form validation that happens during
    // the transformQueue call.  transformQueue is tested later.  It would only be expected to get to
    // submission of an invalid form if `skipValidation=true` was set - meaning we want to capture the incomplete
    // survey results on a step. This can happn on a Dismiss action button, for instance.
    @Test
    fun `submit-form SHOULD call analyticsTracker WHEN form is invalid`() = runTest {
        // GIVEN
        val optionSelect = optionSelect(5, 1)
        val textInput = textInput()
        val formState = formState(optionSelect, textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val action = SubmitFormAction(emptyMap(), RenderContext.Modal, experienceRenderer, analyticsTracker)
        val analyticEvent = StepInteraction(experience, 0, FORM_SUBMITTED)

        // WHEN
        action.execute()

        // THEN
        assertThat(formState.isFormComplete).isFalse()
        verify { analyticsTracker.identify(formState.formattedAsProfileUpdate(), interactive = false) }
        verify { analyticsTracker.track(analyticEvent.name, analyticEvent.properties, interactive = false, isInternal = true) }
    }

    @Test
    fun `submit-form SHOULD transform action queue on invalid form`() = runTest {
        // GIVEN
        val optionSelect = optionSelect(5, 1)
        val textInput = textInput()
        val formState = formState(optionSelect, textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val appcues: Appcues = mockk(relaxed = true)
        val action0 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action = SubmitFormAction(emptyMap(), RenderContext.Modal, experienceRenderer, analyticsTracker)
        val action1 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action2 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val initialQueue = listOf(action0, action, action1, action2)

        // WHEN
        val updatedQueue = action.transformQueue(initialQueue, 1, appcues)

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(1)
        assertThat(updatedQueue[0]).isEqualTo(action0)
    }

    @Test
    fun `submit-form SHOULD NOT transform action queue on valid form`() = runTest {
        // GIVEN
        val optionSelect = optionSelect(5, 1)
        val textInput = textInput()
        val formState = formState(optionSelect, textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val appcues: Appcues = mockk(relaxed = true)
        val action0 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action = SubmitFormAction(emptyMap(), RenderContext.Modal, experienceRenderer, analyticsTracker)
        val action1 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action2 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val initialQueue = listOf(action0, action, action1, action2)

        // WHEN
        formState.setValue(textInput, "text")
        formState.setValue(optionSelect, "0")
        val updatedQueue = action.transformQueue(initialQueue, 1, appcues)

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(4)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isEqualTo(action)
        assertThat(updatedQueue[2]).isEqualTo(action1)
        assertThat(updatedQueue[3]).isEqualTo(action2)
    }

    // test transform queue with invalid form but skipping validation
    @Test
    fun `submit-form SHOULD NOT transform action queue on invalid form if skipValidation=true`() = runTest {
        // GIVEN
        val optionSelect = optionSelect(5, 1)
        val textInput = textInput()
        val formState = formState(optionSelect, textInput)
        val experience = experience(formState)
        val state: State = mockk(relaxed = true) {
            every { this@mockk.currentExperience } returns experience
            every { this@mockk.currentStepIndex } returns 0
        }
        val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
            every { this@mockk.getState(RenderContext.Modal) } returns state
        }
        val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)
        val appcues: Appcues = mockk(relaxed = true)
        val action0 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action = SubmitFormAction(
            mapOf("skipValidation" to true), RenderContext.Modal, experienceRenderer, analyticsTracker
        )
        val action1 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val action2 = TrackEventAction(mapOf("eventName" to "My Custom Event"), appcues)
        val initialQueue = listOf(action0, action, action1, action2)

        // WHEN
        val updatedQueue = action.transformQueue(initialQueue, 1, appcues)

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(4)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isEqualTo(action)
        assertThat(updatedQueue[2]).isEqualTo(action1)
        assertThat(updatedQueue[3]).isEqualTo(action2)
    }

    private fun optionSelect(count: Int = 3, minSelections: Int = 1) =
        OptionSelectPrimitive(
            id = UUID.randomUUID(),
            label = TextPrimitive(id = UUID.randomUUID(), spans = listOf(TextSpanPrimitive("select an option"))),
            minSelections = minSelections.toUInt(),
            selectMode = MULTIPLE,
            options = (0..count).map {
                OptionItem("$it", TextPrimitive(UUID.randomUUID(), spans = listOf(TextSpanPrimitive("$it"))))
            }
        )

    private fun textInput() = TextInputPrimitive(
        id = UUID.randomUUID(),
        label = TextPrimitive(id = UUID.randomUUID(), spans = listOf(TextSpanPrimitive("label"))),
        required = true
    )

    private fun formState(vararg input: ExperiencePrimitive) = ExperienceStepFormState().apply {
        input.forEach { this.register(it) }
    }

    private fun experience(formState: ExperienceStepFormState) = Experience(
        id = UUID.randomUUID(),
        name = "form test",
        stepContainers = listOf(
            StepContainer(
                id = UUID.randomUUID(),
                steps = listOf(
                    Step(
                        id = UUID.randomUUID(),
                        content = mockk(relaxed = true),
                        stepDecoratingTraits = mockk(relaxed = true),
                        actions = emptyMap(),
                        type = "modal",
                        formState = formState,
                        presentingTrait = mockk(relaxed = true),
                        backdropDecoratingTraits = mockk(relaxed = true),
                        containerDecoratingTraits = mockk(relaxed = true),
                        metadataSettingTraits = mockk(relaxed = true)
                    )
                ),
                contentHolderTrait = mockk(relaxed = true),
                contentWrappingTrait = mockk(relaxed = true),
                actions = emptyMap(),
            )
        ),
        published = true,
        priority = NORMAL,
        type = "mobile",
        renderContext = RenderContext.Modal,
        publishedAt = Date().time,
        localeId = null,
        localeName = null,
        completionActions = listOf(),
        trigger = ExperienceTrigger.ShowCall,
        experiment = null,
    )
}
