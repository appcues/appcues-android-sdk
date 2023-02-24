package com.appcues.data.remote.sdksettings

import com.appcues.AppcuesConfig
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse
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
