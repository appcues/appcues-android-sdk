package com.appcues.data.remote.sdksettings

import com.appcues.AppcuesConfig
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.util.ResultOf

internal class SdkSettingsRemoteSource(
    private val service: SdkSettingsService,
    private val config: AppcuesConfig,
) {

    companion object {

        const val BASE_URL = "https://appcues-bundler-development.global.ssl.fastly.net"
    }

    suspend fun getCustomerApiUrl(): ResultOf<String, RemoteError> =
        NetworkRequest.execute {
            service.sdkSettings(config.accountId).services.customerApi.removeSuffix("/")
        }
}
