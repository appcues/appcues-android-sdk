package com.appcues.data.remote.sdksettings

import com.appcues.AppcuesConfig
import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse
import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse.Services
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class SdkSettingsRemoteSourceTest {

    @Test
    fun `sdkSettings SHOULD return Success`() = runTest {
        // GIVEN
        val service = mockk<SdkSettingsService>(relaxed = true)
        val config = mockk<AppcuesConfig>(relaxed = true)
        val source = SdkSettingsRemoteSource(service, config)
        val mockResponse = SdkSettingsResponse(services = Services("customer-api"))
        every { config.accountId } returns "1234"
        coEvery { service.sdkSettings("1234") } returns mockResponse
        // WHEN
        val result = source.sdkSettings()
        // THEN
        assertThat(result).isEqualTo(Success(mockResponse))
    }

    @Test
    fun `sdkSettings SHOULD return Failure`() = runTest {
        // GIVEN
        val service = mockk<SdkSettingsService>(relaxed = true)
        val config = mockk<AppcuesConfig>(relaxed = true)
        val source = SdkSettingsRemoteSource(service, config)
        every { config.accountId } returns "1234"
        coEvery { service.sdkSettings("1234") } throws Exception()
        // WHEN
        val result = source.sdkSettings()
        // THEN
        assertThat(result).isInstanceOf(Failure::class.java)
    }
}
