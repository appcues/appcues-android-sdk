package com.appcues.action

import com.appcues.AppcuesScopeTest
import com.appcues.logging.Logcues
import com.appcues.rules.KoinScopeRule
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.koin.core.component.get

internal class ActionRegistryTest : AppcuesScopeTest {

    @get:Rule
    override val koinTestRule = KoinScopeRule()

    @Test
    fun `get SHOULD return registered ExperienceAction`() {
        // GIVEN
        val type = "myAction"
        val action: ExperienceAction = mockk()
        val registry = ActionRegistry(get())
        registry.register(type) { action }

        // WHEN
        val actionFromRegistry = registry[type]?.invoke(null)

        // THEN
        assertThat(action).isEqualTo(actionFromRegistry)
    }

    @Test
    fun `duplicate registration SHOULD log an error and leave existing`() {
        // GIVEN
        val type = "myAction"
        val action: ExperienceAction = mockk()
        val actionDupe: ExperienceAction = mockk()
        val registry = ActionRegistry(get())
        val logcues: Logcues = get()
        registry.register(type) { action }
        registry.register(type) { actionDupe }

        // WHEN
        val actionFromRegistry = registry[type]?.invoke(null)

        // THEN
        assertThat(action).isEqualTo(actionFromRegistry)
        assertThat(action).isNotEqualTo(actionDupe)
        verify { logcues.error(exception = any()) }
    }
}
