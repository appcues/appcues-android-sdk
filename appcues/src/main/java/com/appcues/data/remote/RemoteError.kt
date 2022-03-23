package com.appcues.data.remote

import com.appcues.data.remote.response.ErrorResponse

internal sealed class RemoteError {
    data class HttpError(val code: Int? = null, val error: ErrorResponse? = null) : RemoteError()
    data class NetworkError(val throwable: Throwable? = null) : RemoteError()
}
