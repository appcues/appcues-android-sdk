package com.appcues.di.scope

import com.appcues.di.definition.Definition
import com.appcues.di.definition.DefinitionException
import com.appcues.di.definition.DefinitionParams
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.UUID

internal class AppcuesScopeTest {

    val scope = AppcuesScope()

    @Test
    fun `scope SHOULD be equal WHEN same scopeId`() {
        // GIVEN
        val scopeId = UUID.randomUUID()
        // WHEN
        val scope1 = AppcuesScope(scopeId)
        val scope2 = AppcuesScope(scopeId)
        // WHEN
        assertThat(scope1).isEqualTo(scope2)
    }

    @Test
    fun `get SHOULD return instance defined in container`() {
        // GIVEN
        val definition = mockk<Definition<String>> {
            every { this@mockk.get(any()) } returns "string"
        }
        scope.define(String::class, definition)
        // WHEN
        val instance = scope.get<String>(DefinitionParams())
        // THEN
        assertThat(instance).isEqualTo("string")
    }

    @Test
    fun `inject SHOULD return instance defined in container`() {
        // GIVEN
        val definition = mockk<Definition<String>> {
            every { this@mockk.get(any()) } returns "string"
        }
        scope.define(String::class, definition)
        // WHEN
        val instance by scope.inject<String>(DefinitionParams())
        // THEN
        assertThat(instance).isEqualTo("string")
    }

    @Test(expected = DefinitionException::class)
    fun `define SHOULD throw if defining same type`() {
        // GIVEN
        val definition = mockk<Definition<String>> {
            every { this@mockk.get(any()) } returns "string"
        }
        scope.define(String::class, definition)
        // WHEN
        scope.define(String::class, definition)
    }

    @Test(expected = DefinitionException::class)
    fun `get SHOULD throw if no type is defined`() {
        // WHEN
        scope.get<String>(DefinitionParams())
    }

    @Test(expected = DefinitionException::class)
    fun `inject SHOULD throw if no type is defined`() {
        // WHEN
        val instance by scope.inject<String>(DefinitionParams())
        // force lazy init
        println(instance)
    }
}
