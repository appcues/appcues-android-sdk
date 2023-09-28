package com.appcues.di

import android.content.Context
import com.appcues.AppcuesConfig
import com.appcues.di.scope.AppcuesScope
import com.appcues.di.scope.AppcuesScopeDSL
import com.appcues.di.scope.get
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence
import org.junit.Test

internal class BootstrapTest {

    @Test
    fun `createScope SHOULD return Scope with scoped Config`() {
        // GIVEN
        val config = AppcuesConfig("1234", "4567")
        // WHEN
        val scope = Bootstrap.createScope(mockk(relaxed = true), config)
        // THEN
        assertThat(scope.get<AppcuesConfig>()).isEqualTo(config)
    }

    @Test
    fun `get SHOULD return existing scope by id`() {
        // GIVEN
        val scope = Bootstrap.createScope(mockk(relaxed = true), AppcuesConfig("1234", "4567"))
        // WHEN
        val scope2 = Bootstrap.get(scope.scopeId.toString())
        // THEN
        assertThat(scope).isEqualTo(scope2)
    }

    @Test
    fun `start SHOULD return scope with scoped app context and self`() {
        // GIVEN
        val appContext = mockk<Context>(relaxed = true)
        val context = mockk<Context> {
            every { applicationContext } returns appContext
        }
        // WHEN
        val scope = Bootstrap.start(context)
        // THEN
        assertThat(scope.get<Context>()).isEqualTo(appContext)
        assertThat(scope.get<AppcuesScope>()).isEqualTo(scope)
    }

    @Test
    fun `start SHOULD invoke dsl for given scope`() {
        // GIVEN
        val context = mockk<Context>(relaxed = true)
        val dsl: (AppcuesScopeDSL.() -> Unit) = mockk(relaxed = true)
        val dslScopeSlot = slot<AppcuesScopeDSL>()
        // WHEN
        val scope = Bootstrap.start(context, listOf(), dsl)
        // THEN
        verify { dsl.invoke(capture(dslScopeSlot)) }
        assertThat(dslScopeSlot.captured.scope).isEqualTo(scope)
    }

    @Test
    fun `start SHOULD return scope with modules installed`() {
        // GIVEN
        val module1 = object : AppcuesModule {
            override fun AppcuesScopeDSL.install() {
                scoped { "test1234" }
            }
        }
        val module2 = object : AppcuesModule {
            override fun AppcuesScopeDSL.install() {
                factory { 12345 }
            }
        }
        val context = mockk<Context>(relaxed = true)
        // WHEN
        val scope = Bootstrap.start(context, listOf(module1, module2))
        // THEN
        assertThat(scope.get<String>()).isEqualTo("test1234")
        assertThat(scope.get<Int>()).isEqualTo(12345)
    }

    @Test
    fun `start SHOULD run module install in sequence`() {
        // GIVEN
        val module1 = mockk<AppcuesModule>(relaxed = true)
        val module2 = mockk<AppcuesModule>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        // WHEN
        val scope = Bootstrap.start(context, listOf(module1, module2))
        // THEN
        verifySequence {
            with(AppcuesScopeDSL(scope)) {
                with(module1) { install() }
                with(module2) { install() }
            }
        }
    }
}
