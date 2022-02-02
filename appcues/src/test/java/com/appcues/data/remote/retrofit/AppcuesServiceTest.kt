package com.appcues.data.remote.retrofit

import com.appcues.data.remote.retrofit.stubs.contentModalOneStubs
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
    fun `content SHOULD fetch Experience correctly from specific path`() {
        // Given
        mockWebServer.dispatchResponses(
            responses = hashMapOf(
                "/v1/accounts/1234/users/TestUser/experience_content/5678" to "content/content_modal_one.json"
            )
        )
        // When
        val result = runBlocking {
            api.content(
                account = "1234",
                user = "TestUser",
                contentId = "5678",
            )
        }
        // Then
        with(result) {
            assertThat(this).isEqualTo(contentModalOneStubs)
        }
    }
}
