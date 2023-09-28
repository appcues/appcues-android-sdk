package com.appcues.di.component

import com.appcues.di.definition.DefinitionParams
import com.appcues.di.definition.FactoryDefinition
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test

internal class FactoryDefinitionTest {

    @Test
    fun `get SHOULD return string instance`() {
        // GIVEN
        val definition = FactoryDefinition {
            return@FactoryDefinition "string"
        }
        // WHEN
        val instance = definition.get(DefinitionParams())
        // THEN
        assertThat(instance).isEqualTo("string")
    }

    @Test
    fun `get SHOULD return string instance WITH param`() {
        // GIVEN
        val definition = FactoryDefinition {
            return@FactoryDefinition "string-${it.next<Int>()}"
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
        val definition = FactoryDefinition(factory)
        val params = DefinitionParams()
        // WHEN
        definition.get(params)
        // THEN
        verify { factory.invoke(params) }
    }

    @Test
    fun `get SHOULD always call`() {
        // GIVEN
        val factory: ((DefinitionParams) -> String) = mockk(relaxed = true)
        val definition = FactoryDefinition(factory)
        val params = DefinitionParams()
        // WHEN
        definition.get(params)
        definition.get(params)
        definition.get(params)
        // THEN
        verifySequence {
            factory.invoke(params)
            factory.invoke(params)
            factory.invoke(params)
        }
    }
}
