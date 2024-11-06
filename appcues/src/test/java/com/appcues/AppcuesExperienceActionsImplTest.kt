package com.appcues

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.action.appcues.CloseAction
import com.appcues.action.appcues.ContinueAction
import com.appcues.action.appcues.TrackEventAction
import com.appcues.action.appcues.UpdateProfileAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.RenderContext
import com.appcues.logging.Logcues
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
internal class AppcuesExperienceActionsImplTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var experienceActions: AppcuesExperienceActionsImpl

    private val renderContext = RenderContext.Modal

    private val actions = listOf<ExperienceAction>()

    private val coroutineScope: CoroutineScope = AppcuesCoroutineScope(Logcues())

    private val analyticsTracker = mockk<AnalyticsTracker>(relaxed = true)

    private val experienceRenderer = mockk<ExperienceRenderer>(relaxed = true)

    private val actionsProcessor = mockk<ActionProcessor>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        experienceActions = AppcuesExperienceActionsImpl(
            identifier = "identifier",
            actions = actions,
            actionsProcessor = actionsProcessor,
            renderContext = renderContext,
            coroutineScope = coroutineScope,
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
            actionsProcessor.process(renderContext, actions, BUTTON_TAPPED, "Custom component identifier")
        }
    }

    @Test
    fun `nextStep SHOULD call experience renderer show targeting the next step`() = runTest {
        // Given
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.nextStep()
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(ContinueAction::class.java)
            assertThat((this[0] as ContinueAction).config).containsEntry("offset", 1)
        }
    }

    @Test
    fun `previousStep SHOULD call experience renderer show targeting the previous step`() = runTest {
        // Given
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.previousStep()
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(ContinueAction::class.java)
            assertThat((this[0] as ContinueAction).config).containsEntry("offset", -1)
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss marking complete`() = runTest {
        // Given
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.close(markComplete = true)
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(CloseAction::class.java)
            assertThat((this[0] as CloseAction).config).containsEntry("markComplete", true)
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss`() = runTest {
        // Given
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.close()
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(CloseAction::class.java)
            assertThat((this[0] as CloseAction).config).containsEntry("markComplete", false)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name and properties`() {
        // Given
        val properties = mapOf("prop1" to "value")
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.track("test", properties)
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(TrackEventAction::class.java)
            assertThat((this[0] as TrackEventAction).config).containsEntry("eventName", "test")
            assertThat((this[0] as TrackEventAction).config).containsEntry("attributes", properties)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name`() = runTest {
        // Given
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.track("test")
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(TrackEventAction::class.java)
            assertThat((this[0] as TrackEventAction).config).containsEntry("eventName", "test")
        }
    }

    @Test
    fun `updateProfile SHOULD call identify with updated properties`() = runTest {
        // Given
        val properties = mapOf("prop1" to "value")
        val actionsSlot = slot<List<ExperienceAction>>()
        coEvery { actionsProcessor.process(capture(actionsSlot)) }
        // When
        experienceActions.updateProfile(properties)
        // Then
        with(actionsSlot.captured) {
            assertThat(size).isEqualTo(1)
            assertThat(this[0]).isInstanceOf(UpdateProfileAction::class.java)
            assertThat((this[0] as UpdateProfileAction).config).containsEntry("prop1", "value")
        }
    }
}
