package com.appcues.data.remote

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.experience.ExperienceResponse

internal interface AppcuesRemoteSource {

    suspend fun getExperienceContent(experienceId: String): ExperienceResponse

    suspend fun postActivity(userId: String, activityJson: String, sync: Boolean): ActivityResponse
}
