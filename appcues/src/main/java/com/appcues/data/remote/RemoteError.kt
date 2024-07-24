package com.appcues.data.remote

import com.appcues.data.remote.appcues.response.ErrorResponse
import com.appcues.data.remote.appcues.response.PushErrorResponse

internal sealed class RemoteError {
    data class HttpError(val code: Int? = null, val error: ErrorResponse? = null) : RemoteError()
    data class HttpErrorV2(val code: Int? = null, val error: PushErrorResponse? = null) : RemoteError()
    data class NetworkError(val throwable: Throwable? = null) : RemoteError()
}
