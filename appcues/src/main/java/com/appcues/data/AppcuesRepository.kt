package com.appcues.data

import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.data.remote.request.ActivityRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppcuesRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val experienceMapper: ExperienceMapper,
) {

    suspend fun getContent(contentId: String): Experience = withContext(Dispatchers.IO) {
        appcuesRemoteSource.getContent(contentId).let {
            experienceMapper.map(it)
        }
    }

    suspend fun trackActivity(activity: ActivityRequest, sync: Boolean) = withContext(Dispatchers.IO) {
        appcuesRemoteSource.postActivity(activity, sync)
    }
}
