package com.appcues.data.remote.sdksettings

import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse
import retrofit2.http.GET
import retrofit2.http.Path

internal interface SdkSettingsService {
    @GET("bundle/accounts/{account}/mobile/settings")
    suspend fun sdkSettings(
        @Path("account") account: String,
    ): SdkSettingsResponse
}
