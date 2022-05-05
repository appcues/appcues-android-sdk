package com.appcues.util

internal sealed class ResultOf<out Success, out Failure> {
    data class Success<out Success>(val value: Success) : ResultOf<Success, Nothing>()
    data class Failure<out Failure>(val reason: Failure) : ResultOf<Nothing, Failure>()
}

internal inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.doIfSuccess(callback: (value: Success) -> Unit) {
    if (this is ResultOf.Success) {
        callback(value)
    }
}

internal inline fun <reified Success, reified Failure> ResultOf<Success, Failure>.doIfFailure(callback: (reason: Failure) -> Unit) {
    if (this is ResultOf.Failure) {
        callback(reason)
    }
}
