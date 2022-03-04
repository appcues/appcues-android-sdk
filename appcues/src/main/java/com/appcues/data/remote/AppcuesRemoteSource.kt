package com.appcues.data.remote

import com.appcues.data.remote.request.ActivityRequest
import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse

internal interface AppcuesRemoteSource {

    suspend fun getContent(contentId: String): ExperienceResponse

    suspend fun postActivity(activity: ActivityRequest, sync: Boolean): ActivityResponse
}
