package com.appcues.data.remote.retrofit

import com.appcues.SessionMonitor
import com.appcues.Storage
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.ErrorResponse
import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.ResultOf
import com.google.gson.Gson
import com.google.gson.JsonParseException
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

internal class RetrofitAppcuesRemoteSource(
    private val appcuesService: AppcuesService,
    private val accountId: String,
    private val storage: Storage,
    private val gson: Gson,
    private val sessionMonitor: SessionMonitor,
) : AppcuesRemoteSource {

    override suspend fun getExperienceContent(experienceId: String): ResultOf<ExperienceResponse, RemoteError> =
        request {
            appcuesService.experienceContent(accountId, storage.userId, experienceId)
        }

    override suspend fun getExperiencePreview(experienceId: String): ResultOf<ExperienceResponse, RemoteError> =
        // preview _can_ be personalized, so attempt to use the user info, if a valid session exists
        if (sessionMonitor.isActive) {
            request {
                appcuesService.experiencePreview(accountId, storage.userId, experienceId)
            }
        } else {
            request {
                appcuesService.experiencePreview(accountId, experienceId)
            }
        }

    override suspend fun postActivity(userId: String, activityJson: String): ResultOf<ActivityResponse, RemoteError> =
        request {
            appcuesService.activity(
                account = accountId,
                user = userId,
                activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }

    override suspend fun qualify(userId: String, activityJson: String): ResultOf<QualifyResponse, RemoteError> =
        request {
            appcuesService.qualify(
                account = accountId,
                user = userId,
                activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }

    suspend fun <Success> request(apiCall: suspend () -> Success): ResultOf<Success, RemoteError> =
        try {
            ResultOf.Success(apiCall.invoke())
        } catch (exception: HttpException) {
            ResultOf.Failure(RemoteError.HttpError(exception.code(), convertErrorBody(exception)))
        } catch (exception: Exception) {
            ResultOf.Failure(RemoteError.NetworkError(exception))
        }

    private fun convertErrorBody(exception: HttpException): ErrorResponse? =
        try {
            exception.response()?.errorBody()?.charStream()?.let { gson.fromJson(it, ErrorResponse::class.java) }
        } catch (exception: JsonParseException) {
            null
        }
}
