package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.data.model.RenderContext
import com.appcues.rules.KoinScopeRule
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

internal class CloseActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `close SHOULD have expected type name`() {
        assertThat(CloseAction.TYPE).isEqualTo("@appcues/close")
    }

    @Test
    fun `category SHOULD be internal`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf(), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.category).isEqualTo("internal")
    }

    @Test
    fun `destination SHOULD be end experience`() {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf(), RenderContext.Modal, experienceRenderer)
        // THEN
        assertThat(action.destination).isEqualTo("end-experience")
    }

    @Test
    fun `close SHOULD call ExperienceRenderer to dismiss current experience`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf(), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.dismiss(RenderContext.Modal, markComplete = false, destroyed = false) }
    }

    @Test
    fun `close SHOULD call ExperienceRenderer to dismiss current experience with markComplete true WHEN config is true`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf("markComplete" to true), RenderContext.Modal, experienceRenderer)

        // WHEN
        action.execute()

        // THEN
        coVerify { experienceRenderer.dismiss(RenderContext.Modal, markComplete = true, destroyed = false) }
    }
}
