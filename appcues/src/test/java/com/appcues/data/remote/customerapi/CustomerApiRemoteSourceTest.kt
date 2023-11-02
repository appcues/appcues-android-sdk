package com.appcues.data.remote.customerapi

import com.appcues.AppcuesConfig
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse
import com.appcues.data.remote.customerapi.response.PreUploadScreenshotResponse.Upload
import com.appcues.util.ContextWrapper
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class CustomerApiRemoteSourceTest {

    private val service = mockk<CustomerApiService>()

    private val config = mockk<AppcuesConfig> {
        every { applicationId } returns "appId-123"
        every { accountId } returns "accountId-1234"
    }

    private val contextWrapper = mockk<ContextWrapper>(relaxed = true)

    private val source = CustomerApiRemoteSource(
        service = service,
        config = config,
        contextWrapper = contextWrapper
    )

    private val getUploadPath = "/v1/accounts/accountId-1234/mobile/appId-123/pre-upload-screenshot"
    private val saveCapturePath = "/v1/accounts/accountId-1234/mobile/appId-123/screens"

    @Test
    fun `getUploadUrls SHOULD success WITH ImageUrls`() = runTest {
        // GIVEN
        val url = "http://www.appcues.com"
        val token = "token-1234"
        val name = "image.png"
        val preUploadResult = PreUploadScreenshotResponse(
            upload = Upload("uploadUrl"),
            url = "finalUrl"
        )
        coEvery { service.preUploadScreenshot(url + getUploadPath, "Bearer $token", name) } returns preUploadResult
        // WHEN
        val result = source.getUploadUrls(url, token, name)
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)
        with(result as Success) {
            assertThat(value.finalUrl).isEqualTo("finalUrl")
            assertThat(value.uploadUrl).isEqualTo("uploadUrl")
        }
    }

    @Test
    fun `getUploadUrls SHOULD failure`() = runTest {
        // GIVEN
        val url = "http://www.appcues.com"
        val token = "token-1234"
        val name = "image.png"
        val exception = Exception("test exception")
        coEvery { service.preUploadScreenshot(url + getUploadPath, "Bearer $token", name) } throws exception
        // WHEN
        val result = source.getUploadUrls(url, token, name)
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }

    @Test
    fun `saveCapture SHOULD success`() = runTest {
        // GIVEN
        val url = "http://www.appcues.com"
        val token = "token-1234"
        val imageUrl = "image-url.com"
        coEvery { service.saveCapture(url + saveCapturePath, "Bearer $token", any()) } returns Unit
        // WHEN
        val result = source.saveCapture(url, token, mockk(relaxed = true), imageUrl)
        // THEN
        assertThat(result).isInstanceOf(Success::class.java)
    }

    @Test
    fun `saveCapture SHOULD fail`() = runTest {
        // GIVEN
        val url = "http://www.appcues.com"
        val token = "token-1234"
        val imageUrl = "image-url.com"
        coEvery { service.saveCapture(url + saveCapturePath, "Bearer $token", any()) } throws Exception()
        // WHEN
        val result = source.saveCapture(url, token, mockk(relaxed = true), imageUrl)
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }
}
