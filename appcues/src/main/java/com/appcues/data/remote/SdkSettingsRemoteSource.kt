package com.appcues.data.remote

import com.appcues.AppcuesConfig
import com.appcues.data.remote.response.settings.SdkSettingsResponse
import com.appcues.data.remote.retrofit.SdkSettingsService
import com.appcues.util.ResultOf

internal class SdkSettingsRemoteSource(
    private val service: SdkSettingsService,
    private val config: AppcuesConfig,
) {
    companion object {
        const val BASE_URL = "https://fast.appcues.com/"
    }

    suspend fun sdkSettings(): ResultOf<SdkSettingsResponse, RemoteError> =
        NetworkRequest.execute {
            service.sdkSettings(config.accountId)
        }
}
