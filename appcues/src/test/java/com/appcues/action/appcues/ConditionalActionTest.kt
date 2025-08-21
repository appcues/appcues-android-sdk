package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.action.ActionRegistry
import com.appcues.data.model.Clause
import com.appcues.data.model.Experience
import com.appcues.data.model.ExperiencePrimitive
import com.appcues.data.model.ExperiencePrimitive.TextInputPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextPrimitive
import com.appcues.data.model.ExperiencePrimitive.TextSpanPrimitive
import com.appcues.data.model.ExperiencePriority.NORMAL
import com.appcues.data.model.ExperienceStepFormState
import com.appcues.data.model.ExperienceTrigger
import com.appcues.data.model.RenderContext
import com.appcues.data.model.Step
import com.appcues.data.model.StepContainer
import com.appcues.data.remote.appcues.response.action.ActionResponse
import com.appcues.di.component.get
import com.appcues.rules.TestScopeRule
import com.appcues.statemachine.State
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

internal class ConditionalActionTest : AppcuesScopeTest {

    @get:Rule
    override val scopeRule = TestScopeRule()

    @Test
    fun `conditional SHOULD have expected type name`() {
        assertThat(ConditionalAction.TYPE).isEqualTo("@appcues/conditional")
    }

    @Test
    fun `conditional SHOULD not transform queue WHEN no checks are satisfied`() = runTest {
        // GIVEN
        val setup = createTestSetup(value = "no")
        val check = Check(
            condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "yes")),
            actions = listOf()
        )

        val action0 = TrackEventAction(mapOf("eventName" to "Before"), get())
        val action1 = ConditionalAction(
            config = mapOf("checks" to listOf(check)),
            renderContext = RenderContext.Modal,
            experienceRenderer = setup.experienceRenderer,
            actionRegistry = setup.actionRegistry,
            logcues = get()
        )
        val action2 = TrackEventAction(mapOf("eventName" to "After"), get())

        // WHEN
        val updatedQueue = action1.transformQueue(listOf(action0, action1, action2), 1, get())

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(3)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isEqualTo(action1)
        assertThat(updatedQueue[2]).isEqualTo(action2)
    }

    @Test
    fun `conditional SHOULD transform queue WHEN first check is satisfied`() = runTest {
        // GIVEN
        val setup = createTestSetup(value = "yes")
        val checks = listOf(
            Check(
                condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "yes")),
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/track", config = mapOf("eventName" to "After")),
                    ActionResponse(on = "tap", type = "@appcues/continue", config = mapOf("stepID" to UUID.randomUUID()))
                )
            ),
            // This 2nd check should be ignored because the first is satisfied
            Check(
                condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.STARTS_WITH, value = "y")),
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/close", config = mapOf("markComplete" to true))
                )
            )
        )

        val action0 = TrackEventAction(mapOf("eventName" to "Before"), get())
        val action1 = ConditionalAction(
            config = mapOf("checks" to checks),
            renderContext = RenderContext.Modal,
            experienceRenderer = setup.experienceRenderer,
            actionRegistry = setup.actionRegistry,
            logcues = get()
        )
        val action2 = TrackEventAction(mapOf("eventName" to "After"), get())

        // WHEN
        val updatedQueue = action1.transformQueue(listOf(action0, action1, action2), 1, get())

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(4)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isInstanceOf(TrackEventAction::class.java)
        assertThat(updatedQueue[2]).isInstanceOf(ContinueAction::class.java)
        assertThat(updatedQueue[3]).isEqualTo(action2)
    }

    @Test
    fun `conditional SHOULD transform queue WHEN second check is satisfied`() = runTest {
        // GIVEN
        val setup = createTestSetup(value = "maybe")
        val checks = listOf(
            Check(
                condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "yes")),
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/track", config = mapOf("eventName" to "After")),
                )
            ),
            Check(
                condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "maybe")),
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/close", config = mapOf("markComplete" to true))
                )
            )
        )

        val action0 = TrackEventAction(mapOf("eventName" to "Before"), get())
        val action1 = ConditionalAction(
            config = mapOf("checks" to checks),
            renderContext = RenderContext.Modal,
            experienceRenderer = setup.experienceRenderer,
            actionRegistry = setup.actionRegistry,
            logcues = get()
        )
        val action2 = TrackEventAction(mapOf("eventName" to "After"), get())

        // WHEN
        val updatedQueue = action1.transformQueue(listOf(action0, action1, action2), 1, get())

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(3)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isInstanceOf(CloseAction::class.java)
        assertThat(updatedQueue[2]).isEqualTo(action2)
    }

    @Test
    fun `conditional SHOULD transform queue WHEN else check is satisfied`() = runTest {
        // GIVEN
        val setup = createTestSetup(value = "no")
        val checks = listOf(
            Check(
                condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "yes")),
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/track", config = mapOf("eventName" to "After")),
                )
            ),
            Check(
                condition = null,
                actions = listOf(
                    ActionResponse(on = "tap", type = "@appcues/close", config = mapOf("markComplete" to true))
                )
            )
        )

        val action0 = TrackEventAction(mapOf("eventName" to "Before"), get())
        val action1 = ConditionalAction(
            config = mapOf("checks" to checks),
            renderContext = RenderContext.Modal,
            experienceRenderer = setup.experienceRenderer,
            actionRegistry = setup.actionRegistry,
            logcues = get()
        )
        val action2 = TrackEventAction(mapOf("eventName" to "After"), get())

        // WHEN
        val updatedQueue = action1.transformQueue(listOf(action0, action1, action2), 1, get())

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(3)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isInstanceOf(CloseAction::class.java)
        assertThat(updatedQueue[2]).isEqualTo(action2)
    }

    @Test
    fun `conditional SHOULD transform queue WHEN actions are empty`() = runTest {
        // GIVEN
        val setup = createTestSetup(value = "yes")
        val check = Check(
            condition = Clause.Survey(Clause.SurveyClause(block = setup.blockID, operator = Clause.Operator.EQUALS, value = "yes")),
            actions = listOf()
        )

        val action0 = TrackEventAction(mapOf("eventName" to "Before"), get())
        val action1 = ConditionalAction(
            config = mapOf("checks" to listOf(check)),
            renderContext = RenderContext.Modal,
            experienceRenderer = setup.experienceRenderer,
            actionRegistry = setup.actionRegistry,
            logcues = get()
        )
        val action2 = TrackEventAction(mapOf("eventName" to "After"), get())

        // WHEN
        val updatedQueue = action1.transformQueue(listOf(action0, action1, action2), 1, get())

        // THEN
        assertThat(updatedQueue.count()).isEqualTo(2)
        assertThat(updatedQueue[0]).isEqualTo(action0)
        assertThat(updatedQueue[1]).isEqualTo(action2)
    }

    private data class TestSetup(
        val blockID: UUID,
        val experienceRenderer: ExperienceRenderer,
        val actionRegistry: ActionRegistry
    )

    private fun createTestSetup(value: String): TestSetup {
        val blockID = UUID.randomUUID()
        val textInput = TextInputPrimitive(
            id = blockID,
            label = TextPrimitive(id = UUID.randomUUID(), spans = listOf(TextSpanPrimitive("label"))),
            defaultValue = value
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
        val actionRegistry = ActionRegistry(get())
        
        return TestSetup(blockID, experienceRenderer, actionRegistry)
    }

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
        workflowId = null,
        workflowTaskId = null,
        completionActions = listOf(),
        trigger = ExperienceTrigger.ShowCall,
        experiment = null,
    )
}
