package com.appcues.util

sealed class Result<out Success, out Failure> {
    data class Success<out Success>(val value: Success) : Result<Success, Nothing>()
    data class Failure<out Failure>(val reason: Failure) : Result<Nothing, Failure>()
}

inline fun <reified Success, reified Failure> Result<Success, Failure>.doIfSuccess(callback: (value: Success) -> Unit) {
    if (this is Result.Success) {
        callback(value)
    }
}

inline fun <reified Success, reified Failure> Result<Success, Failure>.doIfFailure(callback: (reason: Failure) -> Unit) {
    if (this is Result.Failure) {
        callback(reason)
    }
}
