package com.appcues.di.component

import com.appcues.di.definition.DefinitionParams
import com.appcues.di.scope.AppcuesScope
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test

internal class AppcuesComponentTest : AppcuesComponent {

    override val scope: AppcuesScope = mockk(relaxed = true)

    @Test
    fun `get SHOULD call scope get`() {
        // GIVEN
        val paramsSlot = slot<DefinitionParams>()
        // WHEN
        get<String>("param1", 2)
        // THEN
        verify { scope.get(String::class, capture(paramsSlot)) }
        with(paramsSlot.captured) {
            assertThat(next<String>()).isEqualTo("param1")
            assertThat(next<Int>()).isEqualTo(2)
        }
    }

    @Test
    fun `inject SHOULD call scope inject`() {
        // GIVEN
        val paramsSlot = slot<DefinitionParams>()
        // WHEN
        inject<String>("param1", 2)
        // THEN
        verify { scope.inject(String::class, capture(paramsSlot)) }
        with(paramsSlot.captured) {
            assertThat(next<String>()).isEqualTo("param1")
            assertThat(next<Int>()).isEqualTo(2)
        }
    }
}
