package com.appcues.data.remote

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.ResultOf

internal interface AppcuesRemoteSource {
    suspend fun getExperienceContent(experienceId: String): ResultOf<ExperienceResponse, RemoteError>
    suspend fun getExperiencePreview(experienceId: String): ResultOf<ExperienceResponse, RemoteError>
    suspend fun postActivity(userId: String, activityJson: String): ResultOf<ActivityResponse, RemoteError>
    suspend fun qualify(userId: String, activityJson: String): ResultOf<QualifyResponse, RemoteError>
    suspend fun checkAppcuesConnection(): Boolean
}
