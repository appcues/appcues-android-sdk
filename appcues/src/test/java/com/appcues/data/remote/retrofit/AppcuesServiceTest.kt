package com.appcues.data.remote.retrofit

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.step.StepContainerResponse
import com.appcues.data.remote.retrofit.deserializer.ActivityResponseDeserializer
import com.appcues.data.remote.retrofit.deserializer.StepContainerResponseDeserializer
import com.appcues.data.remote.retrofit.serializer.DateSerializer
import com.appcues.data.remote.retrofit.stubs.contentModalOneStubs
import com.google.common.truth.Truth.assertThat
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import java.util.Date

class AppcuesServiceTest {

    private val mockWebServer = MockWebServer()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, DateSerializer())
        .registerTypeAdapter(ActivityResponse::class.java, ActivityResponseDeserializer())
        .registerTypeAdapter(StepContainerResponse::class.java, StepContainerResponseDeserializer())
        .create()

    private val api = RetrofitWrapper(gson, mockWebServer.url("/"), false)
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
