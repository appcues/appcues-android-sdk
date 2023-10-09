package com.appcues.data.remote.sdksettings

import com.appcues.data.remote.sdksettings.response.LocalQualificationResponse
import com.appcues.data.remote.sdksettings.response.SdkSettingsResponse
import retrofit2.http.GET
import retrofit2.http.Path

internal interface BundleService {

    @GET("bundle/accounts/{account}/mobile/settings")
    suspend fun sdkSettings(
        @Path("account") account: String,
    ): SdkSettingsResponse

    @GET("bundle/accounts/{account}/local_qualifications")
    suspend fun getLocalQualifications(
        @Path("account") account: String,
    ): LocalQualificationResponse
}
