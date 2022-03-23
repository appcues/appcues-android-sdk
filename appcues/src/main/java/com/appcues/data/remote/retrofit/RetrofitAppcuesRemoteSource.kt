package com.appcues.data.remote.retrofit

import com.appcues.Storage
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

internal class RetrofitAppcuesRemoteSource(
    private val appcuesService: AppcuesService,
    private val accountId: String,
    private val storage: Storage
) : AppcuesRemoteSource {

    override suspend fun getExperienceContent(experienceId: String): ExperienceResponse =
        appcuesService.experienceContent(accountId, storage.userId, experienceId)

    override suspend fun postActivity(userId: String, activityJson: String, sync: Boolean): ActivityResponse =
        appcuesService.activity(
            account = accountId,
            user = userId,
            sync = if (sync) 1 else null,
            activity = activityJson.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        )
}
