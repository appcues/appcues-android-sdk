package com.appcues.di.scope

import com.appcues.di.definition.Definition
import com.appcues.di.definition.DefinitionParams
import com.appcues.di.definition.FactoryDefinition
import com.appcues.di.definition.ScopedDefinition
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

internal class AppcuesScopeDSLTest {

    val scope = mockk<AppcuesScope>(relaxed = true)

    val dsl = AppcuesScopeDSL(scope)

    @Test
    fun `get SHOULD call scope get`() {
        // GIVEN
        val params = DefinitionParams()
        // WHEN
        dsl.get<String>(params)
        // THEN
        verify { scope.get(String::class, params) }
    }

    @Test
    fun `scoped SHOULD call scope define WITH ScopedDefinition`() {
        // GIVEN
        val factory: ((DefinitionParams) -> String) = mockk(relaxed = true)
        val definitionSlot = slot<Definition<String>>()
        // WHEN
        dsl.scoped(factory)
        // THEN
        verify { scope.define(String::class, capture(definitionSlot)) }
        assertThat(definitionSlot.captured).isInstanceOf(ScopedDefinition::class.java)
    }

    @Test
    fun `factory SHOULD call scope define WITH FactoryDefinition`() {
        // GIVEN
        val factory: ((DefinitionParams) -> String) = mockk(relaxed = true)
        val definitionSlot = slot<Definition<String>>()
        // WHEN
        dsl.factory(factory)
        // THEN
        verify { scope.define(String::class, capture(definitionSlot)) }
        assertThat(definitionSlot.captured).isInstanceOf(FactoryDefinition::class.java)
    }
}
