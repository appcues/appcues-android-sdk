package com.appcues.data.remote

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.ResultOf

internal interface AppcuesRemoteSource {
    suspend fun getExperienceContent(experienceId: String): ResultOf<ExperienceResponse, RemoteError>
    suspend fun postActivity(userId: String, activityJson: String, sync: Boolean): ResultOf<ActivityResponse, RemoteError>
}
