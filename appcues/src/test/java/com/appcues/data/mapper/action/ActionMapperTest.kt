package com.appcues.data.mapper.action

import com.appcues.data.remote.response.action.ActionResponse
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ActionMapperTest {

    private val mapper = ActionMapper()

    @Test
    fun `map SHOULD map ActionResponse into Action WHEN on is tap`() {
        // Given
        val from = ActionResponse(
            on = "tap",
            type = "@appcues/close"
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(on).isEqualTo(com.appcues.data.model.action.OnAction.TAP)
            assertThat(type).isEqualTo("@appcues/close")
        }
    }

    @Test
    fun `map SHOULD map ActionResponse into Action WHEN on is longPress`() {
        // Given
        val from = ActionResponse(
            on = "longPress",
            type = "@appcues/open"
        )
        // When
        val result = mapper.map(from)
        // Then
        with(result) {
            assertThat(on).isEqualTo(com.appcues.data.model.action.OnAction.LONG_PRESS)
            assertThat(type).isEqualTo("@appcues/open")
        }
    }
}
