package com.appcues.data.remote

import com.appcues.data.remote.response.ActivityResponse
import com.appcues.data.remote.response.QualifyResponse
import com.appcues.data.remote.response.experience.ExperienceResponse
import com.appcues.util.ResultOf
import java.util.UUID

internal interface AppcuesRemoteSource {
    suspend fun getExperienceContent(
        experienceId: String,
        userSignature: String?,
    ): ResultOf<ExperienceResponse, RemoteError>

    suspend fun getExperiencePreview(
        experienceId: String,
        userSignature: String?
    ): ResultOf<ExperienceResponse, RemoteError>

    suspend fun postActivity(
        userId: String,
        userSignature: String?,
        activityJson: String
    ): ResultOf<ActivityResponse, RemoteError>

    suspend fun qualify(
        userId: String,
        userSignature: String?,
        requestId: UUID,
        activityJson: String,
    ): ResultOf<QualifyResponse, RemoteError>

    suspend fun checkAppcuesConnection(): Boolean
}
