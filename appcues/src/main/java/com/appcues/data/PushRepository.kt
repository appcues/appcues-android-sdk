package com.appcues.data

import com.appcues.data.remote.DataLogcues
import com.appcues.data.remote.RemoteError
import com.appcues.data.remote.appcues.AppcuesRemoteSource
import com.appcues.util.ResultOf
import com.appcues.util.ResultOf.Failure
import com.appcues.util.ResultOf.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class PushRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val dataLogcues: DataLogcues,
) {

    suspend fun preview(pushId: String, query: Map<String, String>): ResultOf<Unit, RemoteError> =
        withContext(Dispatchers.IO) {
            return@withContext appcuesRemoteSource.previewPush(pushId, query).let {
                when (it) {
                    is Success -> Success(Unit)
                    is Failure -> {
                        dataLogcues.error("Push Preview failed", it.reason.toString())
                        it
                    }
                }
            }
        }

    suspend fun send(pushId: String): ResultOf<Unit, RemoteError> =
        withContext(Dispatchers.IO) {
            return@withContext appcuesRemoteSource.sendPush(pushId).let {
                when (it) {
                    is Success -> Success(Unit)
                    is Failure -> {
                        dataLogcues.error("Push Send failed", it.reason.toString())
                        it
                    }
                }
            }
        }
}
