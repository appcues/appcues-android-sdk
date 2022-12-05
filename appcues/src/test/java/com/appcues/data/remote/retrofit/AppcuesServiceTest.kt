package com.appcues.data.remote.retrofit

import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.data.remote.response.experience.FailedExperienceResponse
import com.appcues.data.remote.retrofit.stubs.contentModalOneStubs
import com.appcues.util.appcuesFormatted
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Test
import java.util.UUID

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
            api.experienceContent(
                account = "1234",
                user = "TestUser",
                experienceId = "5678",
            )
        }
        // Then
        with(result) {
            assertThat(this).isEqualTo(contentModalOneStubs)
        }
    }

    @Test
    fun `qualify SHOULD lossy decode Experiences from specific path`() {
        // Given
        mockWebServer.dispatchResponses(
            responses = hashMapOf(
                "/v1/accounts/1234/users/TestUser/qualify" to "content/content_lossy_decoding.json"
            )
        )

        // When
        val result = runBlocking {
            api.qualify(
                account = "1234",
                user = "TestUser",
                requestId = UUID.randomUUID(),
                activity = "".toRequestBody(),
            )
        }

        // Then
        //
        // this example has 8 experiences
        // 0. invalid experience missing the type on the content block
        // 1. a valid experience
        // 2. unknown json object
        // 3. a valid experience
        // 4. unknown json object
        // 5. invalid experience - step type null
        // 6. a valid experience
        // 7. invalid experience - primitive ID wrong data type (boolean instead of UUID)
        //
        // this should result in 6 items actually getting deserialized
        // 3 valid items - 1, 3 and 6
        // 3 invalid items but with enough to report error - 0, 5 and 7
        with(result) {
            assertThat(this.experiences.count()).isEqualTo(6)

            with(experiences[0] as FailedExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("7bba162e-9846-449c-98c7-6891adc882ea")
                // this is not the greatest error message, but I think it is a result of using the PolymorphicJsonAdapterFactory
                // from Moshi with "type" as the key for the PrimitiveResponse deserialization - it's saying that it cannot find that
                // key. Ideally it would give the actual JSON path like the others below, but I'm not sure if this is possible for this
                // special case.
                assertThat(this.error).isEqualTo("Error parsing Experience JSON data: Missing label for type")
            }

            with(experiences[1] as ExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("05e78601-d7d8-449f-a074-fe3ae1144366")
            }

            with(experiences[2] as ExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("c77a4ba2-2259-47b6-b380-76dac48f1c25")
            }

            with(experiences[3] as FailedExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("6f0db437-1d1e-4d74-af4d-9cb1257b5d46")
                assertThat(this.error)
                    .isEqualTo("Error parsing Experience JSON data: Non-null value 'type' was null at \$.steps[0].children[0].type")
            }

            with(experiences[4] as ExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("092faf58-f52d-457d-88c5-172d128d2c25")
            }

            with(experiences[5] as FailedExperienceResponse) {
                assertThat(this.id.appcuesFormatted()).isEqualTo("c9c11671-f418-451e-9b4a-33d54ed5299f")
                assertThat(this.error)
                    .isEqualTo("Error parsing Experience JSON data: Expected STRING but was true, a java.lang.Boolean," +
                        " at path \$.steps[0].children[0].content.id")
            }
        }
    }
}
