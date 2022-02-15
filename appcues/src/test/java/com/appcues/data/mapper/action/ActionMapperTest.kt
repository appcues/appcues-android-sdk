package com.appcues.data.mapper.action

import com.appcues.data.remote.response.action.ActionResponse
import com.appcues.domain.entity.action.OnAction
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActionMapperTest {

    private val mapper = ActionMapper()

    @Test
    fun `map SHOULD map ActionResponse into Action WHEN on is tap`() {
        // Given
        val from = ActionResponse(
            on = "tap",
            type = "@appcues/close",
            config = null,
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(on).isEqualTo(OnAction.TAP)
            assertThat(type).isEqualTo("@appcues/close")
        }
    }

    @Test
    fun `map SHOULD map ActionResponse into Action WHEN on is longPress`() {
        // Given
        val from = ActionResponse(
            on = "longPress",
            type = "@appcues/open",
            config = null,
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(on).isEqualTo(OnAction.LONG_PRESS)
            assertThat(type).isEqualTo("@appcues/open")
        }
    }
}
