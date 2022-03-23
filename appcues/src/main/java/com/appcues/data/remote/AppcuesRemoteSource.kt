package com.appcues.data.remote

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.Result

internal interface AppcuesRemoteSource {
    suspend fun getExperienceContent(experienceId: String): Result<ExperienceResponse, RemoteError>
    suspend fun postActivity(userId: String, activityJson: String, sync: Boolean): Result<ActivityResponse, RemoteError>
}
