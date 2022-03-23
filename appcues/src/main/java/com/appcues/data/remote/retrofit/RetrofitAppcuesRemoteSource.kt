package com.appcues.data.remote.retrofit

import com.appcues.Storage
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.ErrorResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.Result
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

internal class RetrofitAppcuesRemoteSource(
    private val appcuesService: AppcuesService,
    private val accountId: String,
    private val storage: Storage,
    private val gson: Gson,
) : AppcuesRemoteSource {

    override suspend fun getExperienceContent(experienceId: String): Result<ExperienceResponse, RemoteError> =
        request {
            appcuesService.experienceContent(accountId, storage.userId, experienceId)
        }

    override suspend fun postActivity(userId: String, activityJson: String, sync: Boolean): Result<ActivityResponse, RemoteError> =
        request {
            appcuesService.activity(
                account = accountId,
                user = userId,
                sync = if (sync) 1 else null,
                activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
            )
        }

    suspend fun <Success> request(apiCall: suspend () -> Success): Result<Success, RemoteError> {
        return try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    Result.Failure(RemoteError.HttpError(code, errorResponse))
                }
                else -> {
                    Result.Failure(RemoteError.NetworkError(throwable))
                }
            }
        }
    }

    private fun convertErrorBody(throwable: HttpException): ErrorResponse? {
        return try {
            throwable.response()?.errorBody()?.charStream()?.let {
                gson.fromJson(it, ErrorResponse::class.java)
            }
        } catch (exception: Exception) {
            null
        }
    }
}
