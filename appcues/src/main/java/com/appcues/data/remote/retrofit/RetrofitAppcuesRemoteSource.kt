package com.appcues.data.remote.retrofit

import com.appcues.AppcuesSession
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse

internal class RetrofitAppcuesRemoteSource(
    private val appcuesService: AppcuesService,
    private val accountId: String,
    private val session: AppcuesSession,
) : AppcuesRemoteSource {

    override suspend fun getContent(contentId: String): ExperienceResponse {
        return appcuesService.content(accountId, session.userId, contentId)
    }

    override suspend fun postActivity(activity: ActivityRequest, sync: Boolean): ActivityResponse {
        return appcuesService.activity(accountId, session.userId, if (sync) 1 else null, activity)
    }
}
