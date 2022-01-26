package com.appcues.data.remote

import com.appcues.data.remote.response.TacoResponse
import com.appcues.data.remote.response.experience.ExperienceResponse

internal interface AppcuesRemoteSource {

    suspend fun getTaco(account: Int, user: String): TacoResponse

    suspend fun getContent(contentId: String): ExperienceResponse
}
