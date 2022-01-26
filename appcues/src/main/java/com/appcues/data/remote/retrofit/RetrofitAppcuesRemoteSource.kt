package com.appcues.data.remote.retrofit

import com.appcues.AppcuesSession
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.response.TacoResponse
import com.appcues.data.remote.response.experience.ExperienceResponse

internal class RetrofitAppcuesRemoteSource(
    private val appcuesService: AppcuesService,
    private val accountId: String,
    private val session: AppcuesSession,
) : AppcuesRemoteSource {

    override suspend fun getTaco(account: Int, user: String): TacoResponse {
        return appcuesService.getTaco(account, user)
    }

    override suspend fun getContent(contentId: String): ExperienceResponse {
        return appcuesService.content(accountId, session.user, contentId)
    }
}
