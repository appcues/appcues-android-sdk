package com.appcues

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType.BUTTON_TAPPED
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepReference.StepOffset
import com.appcues.logging.Logcues
import com.appcues.ui.ExperienceRenderer
import io.mockk.coVerify
import io.mockk.mockk
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
internal class AppcuesExperienceActionsTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private lateinit var experienceActions: AppcuesExperienceActions

    private val renderContext = RenderContext.Modal

    private val actions = listOf<ExperienceAction>()

    private val coroutineScope: CoroutineScope = AppcuesCoroutineScope(Logcues())

    private val analyticsTracker = mockk<AnalyticsTracker>(relaxed = true)

    private val experienceRenderer = mockk<ExperienceRenderer>(relaxed = true)

    private val actionsProcessor = mockk<ActionProcessor>(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        experienceActions = AppcuesExperienceActions(
            identifier = "identifier",
            renderContext = renderContext,
            coroutineScope = coroutineScope,
            analyticsTracker = analyticsTracker,
            experienceRenderer = experienceRenderer,
            actions = actions,
            actionsProcessor = actionsProcessor
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
        // When
        experienceActions.nextStep()
        // Then
        coVerify {
            experienceRenderer.show(renderContext, StepOffset(1))
        }
    }

    @Test
    fun `previousStep SHOULD call experience renderer show targeting the previous step`() = runTest {
        // When
        experienceActions.previousStep()
        // Then
        coVerify {
            experienceRenderer.show(renderContext, StepOffset(-1))
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss marking complete`() = runTest {
        // When
        experienceActions.close(true)
        // Then
        coVerify {
            experienceRenderer.dismiss(renderContext, markComplete = true, destroyed = false)
        }
    }

    @Test
    fun `close SHOULD call experience renderer dismiss`() = runTest {
        // When
        experienceActions.close()
        // Then
        coVerify {
            experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = false)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name and properties`() {
        // Given
        val properties = mapOf("prop1" to "value")
        // When
        experienceActions.track("test", properties)
        // Then
        verify {
            analyticsTracker.track("test", properties)
        }
    }

    @Test
    fun `track SHOULD call analyticsTracker track for given name`() {
        // When
        experienceActions.track("test")
        // Then
        verify {
            analyticsTracker.track("test")
        }
    }

    @Test
    fun `updateProfile SHOULD call identify with updated properties`() {
        // Given
        val properties = mapOf("prop1" to "value")
        // When
        experienceActions.updateProfile(properties)
        // Then
        verify {
            analyticsTracker.identify(properties, interactive = false)
        }
    }
}
