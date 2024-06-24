package com.appcues.ui.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.viewModelScope
import com.appcues.AppcuesCoroutineScope
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.analytics.AnalyticsTracker
import com.appcues.analytics.ExperienceLifecycleEvent.StepInteraction.InteractionType
import com.appcues.data.model.Experience
import com.appcues.data.model.RenderContext
import com.appcues.data.model.StepContainer
import com.appcues.data.model.StepReference
import com.appcues.data.model.StepReference.StepGroupPageIndex
import com.appcues.logging.Logcues
import com.appcues.statemachine.State
import com.appcues.statemachine.effects.AwaitDismissEffect
import com.appcues.statemachine.states.BeginningStepState
import com.appcues.statemachine.states.EndingStepState
import com.appcues.statemachine.states.RenderingStepState
import com.appcues.ui.ExperienceRenderer
import com.appcues.ui.presentation.AppcuesViewModel.PresentationBinding
import com.appcues.ui.presentation.AppcuesViewModel.UIState
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Dismissing
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Idle
import com.appcues.ui.presentation.AppcuesViewModel.UIState.Rendering
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
internal class AppcuesViewModelTest {

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()

    private val renderContext: RenderContext = RenderContext.Modal

    private val analyticsTracker: AnalyticsTracker = mockk(relaxed = true)

    private val coroutineScope: CoroutineScope = AppcuesCoroutineScope(Logcues())

    private val experienceStates = MutableSharedFlow<State>(1)
    private val experienceRenderer: ExperienceRenderer = mockk(relaxed = true) {
        coEvery { getStateFlow(renderContext) } returns experienceStates
    }

    private val actionProcessor: ActionProcessor = mockk(relaxed = true)

    private val onDismiss: () -> Unit = mockk(relaxed = true)

    private val tapPassThroughHandler: (Offset) -> Unit = mockk(relaxed = true)

    private lateinit var viewModel: AppcuesViewModel

    private val uiStates = arrayListOf<UIState>()

    private lateinit var stateJob: Job

    private val binding = object : PresentationBinding {
        override val renderContext: RenderContext = this@AppcuesViewModelTest.renderContext

        override fun onDismiss() = onDismiss.invoke()

        override fun onTap(offset: Offset) = tapPassThroughHandler.invoke(offset)
    }

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        viewModel = AppcuesViewModel(
            binding = binding,
            coroutineScope = coroutineScope,
            experienceRenderer = experienceRenderer,
            analyticsTracker = analyticsTracker,
            actionProcessor = actionProcessor,
        ).apply {
            stateJob = viewModelScope.launch { uiState.collect { uiStates.add(it) } }
        }

        // clear getStateFlow from history
        excludeRecords { experienceRenderer.getStateFlow(renderContext) }
    }

    @After
    fun tearDown() {
        stateJob.cancel()

        Dispatchers.resetMain()
    }

    @Test
    fun `first uiState SHOULD be Idle`() = runTest {
        assertThat(uiStates).hasSize(1)
        assertThat(uiStates[0]).isInstanceOf(Idle::class.java)
    }

    @Test
    fun `init SHOULD call onDismiss WHEN getStateFlow is null`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = mockk {
            coEvery { getStateFlow(renderContext) } returns null
        }
        // WHEN
        viewModel = AppcuesViewModel(binding, coroutineScope, experienceRenderer, analyticsTracker, actionProcessor)
        // WHEN
        verifySequence { onDismiss.invoke() }
    }

    @Test
    fun `RenderingStepState SHOULD emit Rendering`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 1234)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        // WHEN
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // THEN
        assertThat(uiStates).hasSize(2)
        with(uiStates[1] as Rendering) {
            assertThat(experience).isEqualTo(mockExperience)
            assertThat(stepContainer).isEqualTo(mockStepContainer)
            assertThat(position).isEqualTo(1234)
            assertThat(flatStepIndex).isEqualTo(0)
            assertThat(metadata).isEqualTo(mockMetadata)
        }
    }

    @Test
    fun `RenderingStepState SHOULD NOT emit Rendering WHEN flatStepIndex get NULL on groupLookup`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            // different index
            every { groupLookup } returns hashMapOf(3 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 1234)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        // WHEN (flatStepIndex is 2 here)
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // THEN
        assertThat(uiStates).hasSize(1)
    }

    @Test
    fun `RenderingStepState SHOULD NOT emit Rendering WHEN flatStepIndex get NULL on stepIndexLookup`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { groupLookup } returns hashMapOf(0 to 0)
            // different index
            every { stepIndexLookup } returns hashMapOf(6 to 1234)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        // WHEN (flatStepIndex is 2 here)
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // THEN
        assertThat(uiStates).hasSize(1)
    }

    @Test
    fun `EndingStep SHOULD emit Dismissing`() = runTest {
        // GIVEN
        val mockAwaitDismissEffect = AwaitDismissEffect(mockk())
        // WHEN
        experienceStates.emit(EndingStepState(mockk(), 0, true, mockAwaitDismissEffect))
        // THEN
        assertThat(uiStates).hasSize(2)
        with(uiStates[1] as Dismissing) {
            assertThat(awaitDismissEffect).isEqualTo(mockAwaitDismissEffect)
        }
    }

    @Test
    fun `EndingStep SHOULD be ignored WHEN uiState is Dismissing`() = runTest {
        // GIVEN
        val mockAwaitDismissEffect = AwaitDismissEffect(mockk())
        val endingStep = EndingStepState(mockk(), 0, true, mockAwaitDismissEffect)
        // this sets current uiState to Dismissing
        experienceStates.emit(endingStep)
        // WHEN
        experienceStates.emit(endingStep)
        // THEN
        assertThat(uiStates).hasSize(2)
        assertThat(uiStates[1]).isInstanceOf(Dismissing::class.java)
    }

    @Test
    fun `RenderingStepState SHOULD be ignored WHEN uiState is Dismissing`() = runTest {
        // GIVEN
        val mockAwaitDismissEffect = AwaitDismissEffect(mockk())
        val endingStep = EndingStepState(mockk(), 0, true, mockAwaitDismissEffect)
        // this sets current uiState to Dismissing
        experienceStates.emit(endingStep)
        // WHEN
        experienceStates.emit(RenderingStepState(mockk(), 0, hashMapOf()))
        // THEN
        assertThat(uiStates).hasSize(2)
        assertThat(uiStates[1]).isInstanceOf(Dismissing::class.java)
    }

    @Test
    fun `onPageChanged SHOULD call show with StepGroupPageIndex WHEN state is Rendering`() = runTest {
        // GIVEN
        val slot = slot<StepReference>()
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 1234)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // WHEN
        viewModel.onPageChanged(2)
        // THEN
        coVerify { experienceRenderer.show(renderContext, capture(slot)) }
        with(slot.captured as StepGroupPageIndex) {
            assertThat(index).isEqualTo(2)
            assertThat(flatStepIndex).isEqualTo(0)
        }
    }

    @Test
    fun `onActivityChanged SHOULD call dismiss WHEN state is Rendering`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 0)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // WHEN
        viewModel.onActivityChanged()
        // THEN
        coVerify { experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = true) }
    }

    @Test
    fun `onActivityChanged SHOULD not call dismiss WHEN state is NOT Rendering`() = runTest {
        // WHEN
        viewModel.onActivityChanged()
        // THEN
        coVerify { experienceRenderer wasNot called }
    }

    @Test
    fun `onActions SHOULD call process`() = runTest {
        // GIVEN
        val actions = listOf<ExperienceAction>()
        val type = InteractionType.BUTTON_TAPPED
        val description = "view description"
        // WHEN
        viewModel.onActions(actions, type, description)
        // THEN
        verify { actionProcessor.process(renderContext, actions, type, description) }
    }

    @Test
    fun `onPageChanged SHOULD NOT call show WHEN State is not Rendering`() = runTest {
        // WHEN
        viewModel.onPageChanged(0)
        // THEN
        coVerify { experienceRenderer wasNot called }
    }

    @Test
    fun `onPageChanged SHOULD NOT call show WHEN position and index match`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 0)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // WHEN
        viewModel.onPageChanged(0)
        // THEN
        coVerify { experienceRenderer wasNot called }
    }

    @Test
    fun `onDismissed SHOULD call onDismiss and callback`() = runTest {
        // GIVEN
        val awaitDismissEffect = mockk<AwaitDismissEffect>(relaxed = true)
        // WHEN
        viewModel.onDismissed(awaitDismissEffect)
        // THEN
        verifySequence {
            onDismiss.invoke()
            awaitDismissEffect.dismissed()
        }
    }

    @Test
    fun `requestDismissal SHOULD call dismiss WHEN state is Rendering AND experience is dismissible`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { allowDismissal(any()) } returns true
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 0)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // WHEN
        viewModel.requestDismissal()
        // THEN
        coVerify { experienceRenderer.dismiss(renderContext, markComplete = false, destroyed = false) }
    }

    @Test
    fun `requestDismissal SHOULD not call experienceRenderer WHEN state is Rendering AND experience is not dismissible`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { allowDismissal(any()) } returns false
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 0)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        val mockMetadata: Map<String, Any?> = hashMapOf()
        experienceStates.emit(RenderingStepState(mockExperience, 0, mockMetadata))
        // WHEN
        viewModel.requestDismissal()
        // THEN
        coVerify { experienceRenderer wasNot Called }
    }

    @Test
    fun `requestDismissal SHOULD not call experienceRenderer WHEN state is not Rendering`() = runTest {
        // GIVEN
        val mockStepContainer: StepContainer = mockk()
        val mockExperience: Experience = mockk(relaxed = true) {
            every { allowDismissal(any()) } returns true
            every { groupLookup } returns hashMapOf(0 to 0)
            every { stepIndexLookup } returns hashMapOf(0 to 0)
            every { stepContainers } returns listOf(mockStepContainer)
        }
        experienceStates.emit(BeginningStepState(mockExperience, 0, true))
        // WHEN
        viewModel.requestDismissal()
        // THEN
        coVerify { experienceRenderer wasNot Called }
    }

    @Test
    fun `onConfigurationChanged SHOULD call experienceRenderer onViewConfigurationChanged`() = runTest {
        // WHEN
        viewModel.onConfigurationChanged()
        // THEN
        coVerify {
            experienceRenderer.onViewConfigurationChanged(renderContext)
        }
    }
}
