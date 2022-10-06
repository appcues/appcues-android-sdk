package com.appcues.action.appcues

import com.appcues.AppcuesScopeTest
import com.appcues.rules.KoinScopeRule
import com.appcues.ui.ExperienceRenderer
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

@OptIn(ExperimentalCoroutinesApi::class)
internal class CloseActionTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `close SHOULD have expected type name`() {
        assertThat(CloseAction.TYPE).isEqualTo("@appcues/close")
    }

    @Test
    fun `close SHOULD call ExperienceRenderer to dismiss current experience`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf(), experienceRenderer)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { experienceRenderer.dismissCurrentExperience(markComplete = false, destroyed = false) }
    }

    @Test
    fun `close SHOULD call ExperienceRenderer to dismiss current experience with markComplete true WHEN config is true`() = runTest {
        // GIVEN
        val experienceRenderer: ExperienceRenderer = get()
        val action = CloseAction(mapOf("markComplete" to true), experienceRenderer)

        // WHEN
        action.execute(get())

        // THEN
        coVerify { experienceRenderer.dismissCurrentExperience(markComplete = true, destroyed = false) }
    }
}
