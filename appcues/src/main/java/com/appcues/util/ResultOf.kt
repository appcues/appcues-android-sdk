package com.appcues.util

sealed class ResultOf<out Success, out Failure> {
    data class Success<out Success>(val value: Success) : ResultOf<Success, Nothing>()
    data class Failure<out Failure>(val reason: Failure) : ResultOf<Nothing, Failure>()
}

inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.doIfSuccess(callback: (value: Success) -> Unit) {
    if (this is ResultOf.Success) {
        callback(value)
    }
}

inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.doIfFailure(callback: (reason: Failure) -> Unit) {
    if (this is ResultOf.Failure) {
        callback(reason)
    }
}
