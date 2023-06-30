package com.appcues.statemachine

import com.appcues.AppcuesCoroutineScope
import com.appcues.AppcuesScopeTest
import com.appcues.action.ActionProcessor
import com.appcues.action.ExperienceAction
import com.appcues.data.model.Action.Trigger.NAVIGATE
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.ExperienceTrigger.Qualification
import com.appcues.data.model.RenderContext
import com.appcues.mocks.mockExperience
import com.appcues.mocks.mockExperienceNavigateActions
import com.appcues.rules.KoinScopeRule
import com.appcues.rules.MainDispatcherRule
import com.appcues.statemachine.Action.EndExperience
import com.appcues.statemachine.Action.RenderStep
import com.appcues.statemachine.Action.ReportError
import com.appcues.statemachine.Action.Reset
import com.appcues.statemachine.Action.StartExperience
import com.appcues.statemachine.Action.StartStep
import com.appcues.statemachine.Error.ExperienceAlreadyActive
import com.appcues.statemachine.Error.ExperienceError
import com.appcues.statemachine.Error.StepError
import com.appcues.statemachine.State.BeginningExperience
import com.appcues.statemachine.State.BeginningStep
import com.appcues.statemachine.State.EndingExperience
import com.appcues.statemachine.State.EndingStep
import com.appcues.statemachine.State.Idling
import com.appcues.statemachine.State.RenderingStep
import com.appcues.statemachine.StepReference.StepId
import com.appcues.statemachine.StepReference.StepIndex
import com.appcues.statemachine.StepReference.StepOffset
import com.appcues.trait.AppcuesTraitException
import com.appcues.trait.PresentingTrait
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get
import java.util.UUID
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
internal class StateMachineTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state SHOULD be Idling`() {
        // GIVEN
        val experience = mockExperience { }
        val stateMachine = StateMachine(get(), get(), experience)

        // THEN
        assertThat(stateMachine.state).isEqualTo(Idling(experience))
    }

    // Standard Transitions

    @Test
    fun `Idling SHOULD transition to RenderingStep WHEN action is StartExperience`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val initialState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)
        val targetState = RenderingStep(experience, 0, true)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(presented).isTrue()
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `RenderingStep SHOULD transition to RenderingStep in the same group WHEN action is StartStep with step in same group`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val initialState = RenderingStep(experience, 0, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(1))
        val targetState = RenderingStep(experience, 1, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(presented).isFalse() // same container, no present
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `RenderingStep SHOULD transition to RenderingStep in new group WHEN action is StartStep with step in new group`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val initialState = RenderingStep(experience, 2, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(1))
        val targetState = RenderingStep(experience, 3, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(presented).isTrue() // new container
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `RenderingStep SHOULD transition to RenderingStep in new group WHEN action is StartStep with step ID new group`() = runTest {
        // GIVEN
        var presented = false
        val experience = mockExperience { presented = true }
        val initialState = RenderingStep(experience, 2, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepId(UUID.fromString("0f6cda9d-17f0-4c0d-b8e7-e4fb94a128d9")))
        val targetState = RenderingStep(experience, 3, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(presented).isTrue() // new container
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `Idling SHOULD transition to Idling WHEN action is StartExperience AND present throws`() = runTest {
        // this happens when a presenting trait fails to present and throws AppcuesTraitException instead

        // GIVEN
        val experience = mockExperience { throw AppcuesTraitException("test trait exception") }
        val initialState = Idling(experience)
        val targetState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(stateMachine.state).isEqualTo(targetState)
        assertThat(result.failureReason()).isInstanceOf(ExperienceError::class.java)
        with(result.failureReason() as ExperienceError) {
            assertThat(message).isEqualTo("test trait exception")
        }
    }

    @Test
    fun `RenderingStep SHOULD transition to Idling WHEN action is EndExperience with destroyed false`() = runTest {
        // the @appcues/close action would do this

        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = EndExperience(destroyed = false, markComplete = false)
        val targetState = Idling(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `RenderingStep SHOULD transition to Idling WHEN action is EndExperience with destroyed true`() = runTest {
        // the experience Activity being destroyed externally would do this - ex: deep link elsewhere

        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = EndExperience(destroyed = true, markComplete = false)
        val targetState = Idling(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `RenderingStep SHOULD transition to Idling WHEN action is StartStep with offset 1 and already on the last step`() = runTest {
        // continue with offset:1 acts just like a close, when already on the last step

        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 3, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(1))
        val targetState = Idling(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
    }

    @Test
    fun `StartStep SHOULD execute navigation actions sequentially WHEN moving to a new group`() = runTest {
        // Test that a qualified experience with "navigate" actions on a group at index > 0 has those actions executed and
        // completed prior to presenting the container for the next group

        // GIVEN
        val experienceAction1 = mockk<ExperienceAction>(relaxed = true)
        val experienceAction2 = mockk<ExperienceAction>(relaxed = true)
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val navigationActions = listOf(
            com.appcues.data.model.Action(NAVIGATE, experienceAction1),
            com.appcues.data.model.Action(NAVIGATE, experienceAction2),
        )
        val experience = mockExperienceNavigateActions(navigationActions, presentingTrait, Qualification("screen_view"))
        val initialState = RenderingStep(experience, 0, true)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(1))
        val targetState = RenderingStep(experience, 1, false)
        val actionProcessor: ActionProcessor = get()

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
        coVerifyOrder {
            actionProcessor.process(listOf(experienceAction1, experienceAction2))
            presentingTrait.present()
        }
    }

    @Test
    fun `StartExperience SHOULD NOT execute navigation actions WHEN trigger is Qualification`() = runTest {
        // Test that a qualified experience with "navigate" actions on a group at index 0 has those actions ignored
        // when presenting the first step - since flow settings for qualify determine its location

        // GIVEN
        val experienceAction1 = mockk<ExperienceAction>(relaxed = true)
        val experienceAction2 = mockk<ExperienceAction>(relaxed = true)
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val navigationActions = listOf(
            com.appcues.data.model.Action(NAVIGATE, experienceAction1),
            com.appcues.data.model.Action(NAVIGATE, experienceAction2),
        )
        val experience = mockExperienceNavigateActions(navigationActions, presentingTrait, Qualification("screen_view"))
        val initialState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)
        val targetState = RenderingStep(experience, 0, true)
        val actionProcessor: ActionProcessor = get()

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
        coVerify(exactly = 0) { actionProcessor.process(listOf(experienceAction1, experienceAction2)) }
        coVerify { presentingTrait.present() }
    }

    @Test
    fun `StartExperience SHOULD execute navigation actions WHEN trigger is NOT Qualification`() = runTest {
        // Test that a manually triggered experience with "navigate" actions on a group at index 0 has those actions executed
        // when presenting the first step

        // GIVEN
        val experienceAction1 = mockk<ExperienceAction>(relaxed = true)
        val experienceAction2 = mockk<ExperienceAction>(relaxed = true)
        val presentingTrait = mockk<PresentingTrait>(relaxed = true)
        val navigationActions = listOf(
            com.appcues.data.model.Action(NAVIGATE, experienceAction1),
            com.appcues.data.model.Action(NAVIGATE, experienceAction2),
        )
        val experience = mockExperienceNavigateActions(navigationActions, presentingTrait, ExperienceTrigger.Preview)
        val initialState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)
        val targetState = RenderingStep(experience, 0, true)
        val actionProcessor: ActionProcessor = get()

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(targetState)
        assertThat(stateMachine.state).isEqualTo(targetState)
        verify { experienceAction1 wasNot Called }
        verify { experienceAction2 wasNot Called }
        coVerifyOrder {
            actionProcessor.process(listOf(experienceAction1, experienceAction2))
            presentingTrait.present()
        }
    }

    // Error Transitions

    @Test
    fun `Idling SHOULD NOT transition WHEN action is StartExperience with no steps`() = runTest {
        // GIVEN
        val experience = Experience(
            id = UUID.randomUUID(),
            name = "Empty Experience",
            stepContainers = listOf(),
            published = true,
            priority = NORMAL,
            renderContext = RenderContext.Modal,
            publishedAt = 1652895835000,
            completionActions = arrayListOf(),
            experiment = null,
            trigger = ExperienceTrigger.ShowCall,
        )
        val initialState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(ExperienceError::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `Idling SHOULD NOT transition WHEN action is StartExperience with error`() = runTest {
        // GIVEN
        val experience = Experience(
            id = UUID.randomUUID(),
            name = "Failed Experience",
            stepContainers = listOf(),
            published = true,
            priority = NORMAL,
            renderContext = RenderContext.Modal,
            publishedAt = 1652895835000,
            completionActions = arrayListOf(),
            experiment = null,
            error = "Failed decode",
            trigger = ExperienceTrigger.ShowCall,
        )
        val initialState = Idling(experience)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(stateMachine.state).isEqualTo(initialState)
        assertThat((result.failureReason() as ExperienceError).message).isEqualTo("Failed decode")
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is StartExperience`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = StartExperience(experience)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(ExperienceAlreadyActive::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is StartStep with invalid index`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepIndex(1000))

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(StepError::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is StartStep with offset other than 1 on the last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 3, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(2))

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(StepError::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is StartStep with index on the last step`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 3, false)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepIndex(1000))

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(StepError::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `EndingStep SHOULD NOT transition WHEN action is StartStep with invalid index`() = runTest {
        // Note: in actual usage, the invalid StepReference should be caught before transitioning to EndingStep

        // GIVEN
        val experience = mockExperience()
        val initialState = EndingStep(experience, 1, true, null)
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepIndex(1000))

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isInstanceOf(StepError::class.java)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is ReportError AND non-fatal`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val error = ExperienceError(experience, "test error")
        val action = ReportError(error, false)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isEqualTo(error)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `RenderingStep SHOULD transition to Idling WHEN action is ReportError AND is fatal`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val error = ExperienceError(experience, "test error")
        val action = ReportError(error, true)

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.failureReason()).isEqualTo(error)
        assertThat(stateMachine.state).isEqualTo(Idling(experience))
    }

    @Test
    fun `RenderingStep SHOULD NOT transition WHEN action is Reset`() = runTest {
        // Invalid action for given state is just a no-op, Success result with same state

        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = Reset

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    // Testing SharedFlow observation used by UI and Analytics

    @Test
    fun `ReportError action SHOULD emit error to shared flow`() = runTest {
        // GIVEN
        val errorCompletable: CompletableDeferred<Error> = CompletableDeferred()
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val stateMachine = initMachine(initialState, onError = { errorCompletable.complete(it) })
        val error = ExperienceError(experience, "test error")
        val action = ReportError(error, false)

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        assertThat(errorCompletable.await(1.seconds)).isEqualTo(error)
    }

    @Test
    fun `Idling SHOULD emit three state updates to shared flow WHEN action is StartExperience`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = Idling(experience)
        val transitions = mutableListOf(BeginningExperience::class.java, BeginningStep::class.java, RenderingStep::class.java)
        val completion: CompletableDeferred<Boolean> = CompletableDeferred()
        val stateMachine = initMachine(initialState, onStateChange = { confirmTransitions(it, transitions, completion) })
        val action = StartExperience(experience)

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        assertThat(completion.await(1.seconds)).isTrue()
    }

    @Test
    fun `RenderingStep SHOULD emit three state updates to shared flow WHEN action is EndExperience`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val transitions = mutableListOf(EndingStep::class.java, EndingExperience::class.java, Idling::class.java)
        val completion: CompletableDeferred<Boolean> = CompletableDeferred()
        val stateMachine = initMachine(initialState, onStateChange = { confirmTransitions(it, transitions, completion) })
        val action = EndExperience(destroyed = false, markComplete = false)

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        assertThat(completion.await(1.seconds)).isTrue()
    }

    @Test
    fun `RenderingStep SHOULD emit three state updates to shared flow WHEN action is StartStep`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = RenderingStep(experience, 1, false)
        val transitions = mutableListOf(EndingStep::class.java, BeginningStep::class.java, RenderingStep::class.java)
        val completion: CompletableDeferred<Boolean> = CompletableDeferred()
        val stateMachine = initMachine(initialState, onStateChange = { confirmTransitions(it, transitions, completion) })
        val action = StartStep(StepOffset(1))

        // WHEN
        stateMachine.handleAction(action)

        // THEN
        assertThat(completion.await(1.seconds)).isTrue()
    }

    // No-op transitions - ignored
    @Test
    fun `Idling SHOULD NOT transition WHEN action is something other than StartExperience or Pause`() = runTest {
        // GIVEN
        val initialState = Idling(mockExperience())
        val stateMachine = initMachine(initialState)
        val action = Reset

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `BeginningExperience SHOULD NOT transition WHEN action is something other than StartStep or Pause`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = BeginningExperience(experience)
        val stateMachine = initMachine(initialState)
        val action = RenderStep

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `BeginningStep SHOULD NOT transition WHEN action is something other than RenderStep or Pause`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = BeginningStep(experience, 1, false) { }
        val stateMachine = initMachine(initialState)
        val action = StartStep(StepOffset(1))

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `EndingStep SHOULD NOT transition WHEN action is something other than EndExperience, Start Step or Pause`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = EndingStep(experience, 0, false, null)
        val stateMachine = initMachine(initialState)
        val action = Reset

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `EndingExperience SHOULD NOT transition WHEN action is something other than Reset or Pause`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = EndingExperience(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = RenderStep

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(initialState)
        assertThat(stateMachine.state).isEqualTo(initialState)
    }

    @Test
    fun `EndingExperience SHOULD call action processor WHEN experience is completed`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = EndingExperience(experience, 1, true)
        val stateMachine = initMachine(initialState)
        val action = Reset

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(Idling(experience))
        assertThat(stateMachine.state).isEqualTo(Idling(experience))
        coVerify {
            koinTestRule.scope.get<ActionProcessor>().process(experience.completionActions)
        }
    }

    @Test
    fun `EndingExperience SHOULD NOT call action processor WHEN experience is NOT completed`() = runTest {
        // GIVEN
        val experience = mockExperience()
        val initialState = EndingExperience(experience, 1, false)
        val stateMachine = initMachine(initialState)
        val action = Reset

        // WHEN
        val result = stateMachine.handleAction(action)

        // THEN
        assertThat(result.successValue()).isEqualTo(Idling(experience))
        assertThat(stateMachine.state).isEqualTo(Idling(experience))
        coVerify { koinTestRule.scope.get<ActionProcessor>() wasNot Called }
    }

    // Helpers
    private suspend fun initMachine(
        state: State,
        onStateChange: ((State) -> Unit)? = null,
        onError: ((Error) -> Unit)? = null
    ): StateMachine {
        val stateFlowCompletion: CompletableDeferred<Boolean> = CompletableDeferred()
        val errorFlowCompletion: CompletableDeferred<Boolean> = CompletableDeferred()
        val scope: AppcuesCoroutineScope = get()
        val machine = StateMachine(scope, get(), state.experience, state)
        // this collect on the stateFlow simulates the function of the UI
        // that is required to progress the state machine forward on UI present/dismiss
        scope.launch {
            stateFlowCompletion.complete(true)
            machine.stateFlow.collect {
                onStateChange?.invoke(it)
                when (it) {
                    is BeginningStep -> {
                        it.presentationComplete.invoke(Success(Unit))
                    }
                    is EndingStep -> {
                        it.dismissAndContinue?.invoke()
                    }
                    // ignore other state changes
                    else -> Unit
                }
            }
        }
        scope.launch {
            errorFlowCompletion.complete(true)
            machine.errorFlow.collect {
                onError?.invoke((it))
            }
        }
        // this await is to try to ensure that the collect handlers inside the coroutine launches above execute
        // before the test that relies on them gets the state machine instance and tries to validate flows
        awaitAll(stateFlowCompletion, errorFlowCompletion)
        return machine
    }

    // helper that processes a list of sequential expected transitions, and triggers the given completion if all have been
    // observed during the transition flow
    private fun confirmTransitions(state: State, transitions: MutableList<Class<out State>>, completion: CompletableDeferred<Boolean>) {
        if (state::class.java == transitions.first()) transitions.removeAt(0)
        if (transitions.isEmpty()) completion.complete(true)
    }
}

internal inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.successValue(): Success? =
    if (this is ResultOf.Success) value else null

internal inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.failureReason(): Failure? =
    if (this is ResultOf.Failure) reason else null

internal suspend inline fun <reified T> CompletableDeferred<T>.await(duration: kotlin.time.Duration): T =
    withTimeout(duration) { await() }
