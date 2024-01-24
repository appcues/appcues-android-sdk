package com.appcues.data.remote.appcues

import com.appcues.AppcuesConfig
import com.appcues.Storage
import com.appcues.data.remote.NetworkRequest
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.appcues.response.ActivityResponse
import com.appcues.data.remote.appcues.response.QualifyResponse
import com.appcues.data.remote.appcues.response.experience.ExperienceResponse
import com.appcues.util.ResultOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.UUID

internal class AppcuesRemoteSource(
    private val service: AppcuesService,
    private val config: AppcuesConfig,
    private val storage: Storage,
) {

    companion object {

        const val BASE_URL = "https://api.appcues.net/"

        // we should not show an experience response if it takes > 5 seconds to return
        // as it could be out of date with the content the user is now viewing
        const val READ_TIMEOUT_SECONDS: Long = 5
    }

    suspend fun getExperienceContent(
        experienceId: String,
        userSignature: String?,
        query: Map<String, String>
    ): ResultOf<ExperienceResponse, RemoteError> {
        return NetworkRequest.execute {
            service.experienceContent(config.accountId, storage.userId, experienceId, query, userSignature?.let { "Bearer $it" })
        }
    }

    suspend fun getExperiencePreview(
        experienceId: String,
        userSignature: String?,
        query: Map<String, String>
    ): ResultOf<ExperienceResponse, RemoteError> {
        // preview _can_ be personalized, so attempt to use the user info, if a valid userId exists
        return if (storage.userId.isNotEmpty()) {
            NetworkRequest.execute {
                service.experiencePreview(config.accountId, storage.userId, experienceId, query, userSignature?.let { "Bearer $it" })
            }
        } else {
            NetworkRequest.execute {
                service.experiencePreview(config.accountId, experienceId, query)
            }
        }
    }

    suspend fun postActivity(
        userId: String,
        userSignature: String?,
        activityJson: String,
    ): ResultOf<ActivityResponse, RemoteError> =
        NetworkRequest.execute {
            service.activity(
                account = config.accountId,
                user = userId,
                authorization = userSignature?.let { "Bearer $it" },
                activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }

    suspend fun qualify(
        userId: String,
        userSignature: String?,
        requestId: UUID,
        activityJson: String,
    ): ResultOf<QualifyResponse, RemoteError> =
        NetworkRequest.execute {
            service.qualify(
                account = config.accountId,
                user = userId,
                requestId = requestId,
                authorization = userSignature?.let { "Bearer $it" },
                activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }

    suspend fun checkAppcuesConnection(): Boolean {
        return NetworkRequest.execute {
            service.healthCheck()
        }.let {
            when (it) {
                is ResultOf.Failure -> false
                is ResultOf.Success -> it.value.ok
            }
        }
    }
}
