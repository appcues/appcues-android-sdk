package com.appcues.di.component

import com.appcues.di.definition.DefinitionParams
import com.appcues.di.definition.ScopedDefinition
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test
import java.util.UUID

internal class ScopedDefinitionTest {

    @Test
    fun `get SHOULD return string instance`() {
        // GIVEN
        val definition = ScopedDefinition {
            return@ScopedDefinition "string"
        }
        // WHEN
        val instance = definition.get(DefinitionParams())
        // THEN
        assertThat(instance).isEqualTo("string")
    }

    @Test
    fun `get SHOULD return string instance WITH param`() {
        // GIVEN
        val definition = ScopedDefinition {
            return@ScopedDefinition "string-${it.next<Int>()}"
        }
        // WHEN
        val instance = definition.get(DefinitionParams(listOf(2)))
        // THEN
        assertThat(instance).isEqualTo("string-2")
    }

    @Test
    fun `get SHOULD call factory`() {
        // GIVEN
        val factory: ((DefinitionParams) -> String) = mockk(relaxed = true)
        val definition = ScopedDefinition(factory)
        val params = DefinitionParams()
        // WHEN
        definition.get(params)
        // THEN
        verify { factory.invoke(params) }
    }

    @Test
    fun `get SHOULD call factory only once`() {
        // GIVEN
        val factory: ((DefinitionParams) -> String) = mockk(relaxed = true)
        val definition = ScopedDefinition(factory)
        val params = DefinitionParams()
        // WHEN
        definition.get(params)
        definition.get(params)
        definition.get(params)
        // THEN
        verifySequence { factory.invoke(params) }
    }

    @Test
    fun `get SHOULD get same instance`() {
        // GIVEN
        val instance = mockk<UUID>(relaxed = true)
        val factory: ((DefinitionParams) -> UUID) = { instance }
        val definition = ScopedDefinition(factory)
        val params = DefinitionParams()
        // WHEN
        val instance1 = definition.get(params)
        val instance2 = definition.get(params)
        val instance3 = definition.get(params)
        // THEN
        assertThat(instance1).isEqualTo(instance)
        assertThat(instance2).isEqualTo(instance)
        assertThat(instance3).isEqualTo(instance)
    }
}
