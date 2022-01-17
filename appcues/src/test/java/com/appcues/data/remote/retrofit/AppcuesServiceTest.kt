package com.appcues.data.remote.retrofit

import com.appcues.data.remote.retrofit.experiences.modalOneStub
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test

class AppcuesServiceTest {

    private val mockWebServer = MockWebServer()

    private val api = RetrofitWrapper(mockWebServer.url("/"), false)
        .create(AppcuesService::class)

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `getTaco SHOULD fetch taco correctly given 200 response`() = runBlocking {
        // Given
        mockWebServer.enqueueResponse("experiences/modal_one.json", 200)
        // When
        val result = api.getTaco(1234, "TestUser")
        // Then
        with(result) {
            assertThat(this).isEqualTo(modalOneStub)
        }
    }

    @Test
    fun `getTaco(1234, 5678) SHOULD fetch taco correctly from specific path `() = runBlocking {
        // Given
        mockWebServer.dispatchResponses(
            responses = hashMapOf(
                "/v1/accounts/1234/users/5678/activity" to "experiences/modal_one.json"
            )
        )
        // When
        val result = api.getTaco(1234, "TestUser")
        // Then
        with(result) {
            assertThat(this).isEqualTo(modalOneStub)
        }
    }
}
