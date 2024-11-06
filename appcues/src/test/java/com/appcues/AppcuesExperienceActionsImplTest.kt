package com.appcues

import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.RenderContext
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test

internal class AppcuesExperienceActionsImplTest {

    private lateinit var experienceActions: AppcuesExperienceActionsImpl

    private val renderContext = RenderContext.Modal

    private val actions = listOf<ExperienceAction>()

    private val analyticsTracker = mockk<AnalyticsTracker>(relaxed = true)

    private val experienceRenderer = mockk<ExperienceRenderer>(relaxed = true)

    private val actionsProcessor = mockk<ActionProcessor>(relaxed = true)

    @Before
    fun setUp() {
        experienceActions = AppcuesExperienceActionsImpl(
            identifier = "identifier",
            actions = actions,
            actionsProcessor = actionsProcessor,
            renderContext = renderContext,
            analyticsTracker = analyticsTracker,
            experienceRenderer = experienceRenderer
        )
    }

    @Test
    fun `triggerBlockActions SHOULD process list of actions`() {
        // When
        experienceActions.triggerBlockActions()
        // Then
        verify {
            actionsProcessor.enqueue(renderContext, actions, BUTTON_TAPPED, "Custom component identifier")
        }
    }

    @Test
    fun `nextStep SHOULD call experience renderer show targeting the next step`() {
        // Given
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.nextStep()
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(ContinueAction::class.java)
            assertThat((this as ContinueAction).config).containsEntry("offset", 1)
        }
    }

    @Test
    fun `previousStep SHOULD call experience renderer show targeting the previous step`() {
        // Given
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.previousStep()
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(ContinueAction::class.java)
            assertThat((this as ContinueAction).config).containsEntry("offset", -1)
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss marking complete`() {
        // Given
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.close(markComplete = true)
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(CloseAction::class.java)
            assertThat((this as CloseAction).config).containsEntry("markComplete", true)
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss`() {
        // Given
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.close()
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(CloseAction::class.java)
            assertThat((this as CloseAction).config).containsEntry("markComplete", false)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name and properties`() {
        // Given
        val properties = mapOf("prop1" to "value")
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.track("test", properties)
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(TrackEventAction::class.java)
            assertThat((this as TrackEventAction).config).containsEntry("eventName", "test")
            assertThat(this.config).containsEntry("attributes", properties)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name`() {
        // Given
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.track("test")
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(TrackEventAction::class.java)
            assertThat((this as TrackEventAction).config).containsEntry("eventName", "test")
        }
    }

    @Test
    fun `updateProfile SHOULD call identify with updated properties`() {
        // Given
        val properties = mapOf("prop1" to "value")
        val actionSlot = slot<ExperienceAction>()
        every { actionsProcessor.enqueue(capture(actionSlot)) } returns Unit
        // When
        experienceActions.updateProfile(properties)
        // Then
        with(actionSlot.captured) {
            assertThat(this).isInstanceOf(UpdateProfileAction::class.java)
            assertThat((this as UpdateProfileAction).config).containsEntry("prop1", "value")
        }
    }
}
