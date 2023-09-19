package com.appcues.data.remote.customerapi

import com.appcues.AppcuesConfig
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse.Upload
import com.appcues.debugger.screencapture.Capture
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.util.UUID

internal class CustomerApiRemoteSourceTest {

    @Test
    fun `preUploadScreenshot SHOULD return Success`() = runTest {
        // GIVEN
        val service = mockk<CustomerApiService>(relaxed = true)
        val config = mockk<AppcuesConfig>(relaxed = true)
        val source = CustomerApiRemoteSource(service, config)
        val randomId = UUID.randomUUID()
        val capture = mockk<Capture>(relaxed = true) {
            every { id } returns randomId
        }
        val mockResponse = PreUploadScreenshotResponse(upload = Upload("presignedUrl"), "url")
        every { config.accountId } returns "1234"
        every { config.applicationId } returns "5678"
        coEvery { service.preUploadScreenshot("1234", "5678", "$randomId.png", "Bearer auth-token") } returns mockResponse
        // WHEN
        val result = source.preUploadScreenshot(capture, "auth-token")
        // THEN
        assertThat(result).isEqualTo(Success(mockResponse))
    }
}
