package com.appcues.data.remote

import com.appcues.data.MoshiConfiguration
import com.appcues.data.remote.appcues.response.ErrorResponse
import com.appcues.data.remote.appcues.response.PushErrorResponse
import com.appcues.util.ResultOf
import com.squareup.moshi.JsonDataException
import retrofit2.HttpException

internal object NetworkRequest {
    internal suspend fun <Success> execute(apiCall: suspend () -> Success): ResultOf<Success, RemoteError> =
        try {
            ResultOf.Success(apiCall.invoke())
        } catch (exception: HttpException) {
            ResultOf.Failure(RemoteError.HttpError(exception.code(), convertErrorBody(exception)))
        } catch (exception: Exception) {
            ResultOf.Failure(RemoteError.NetworkError(exception))
        }

    private fun convertErrorBody(exception: HttpException): ErrorResponse? =
        try {
            exception.response()?.errorBody()?.source()?.let {
                MoshiConfiguration.moshi.adapter(ErrorResponse::class.java).fromJson(it)
            }
        } catch (exception: JsonDataException) {
            null
        }

    internal suspend fun <Success> executePushRequest(apiCall: suspend () -> Success): ResultOf<Success, RemoteError> =
        try {
            ResultOf.Success(apiCall.invoke())
        } catch (exception: HttpException) {
            ResultOf.Failure(RemoteError.HttpErrorV2(exception.code(), convertPushErrorBody(exception)))
        } catch (exception: Exception) {
            ResultOf.Failure(RemoteError.NetworkError(exception))
        }

    private fun convertPushErrorBody(exception: HttpException): PushErrorResponse? =
        try {
            exception.response()?.errorBody()?.source()?.let {
                MoshiConfiguration.moshi.adapter(PushErrorResponse::class.java).fromJson(it)
            }
        } catch (exception: JsonDataException) {
            null
        }
}
