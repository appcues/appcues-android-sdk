package com.appcues.data

import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.model.Experience
import com.appcues.data.remote.AppcuesRemoteSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AppcuesRepository(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val experienceMapper: ExperienceMapper = ExperienceMapper(),
) {

    suspend fun getContent(contentId: String): Experience = withContext(Dispatchers.IO) {
        appcuesRemoteSource.getContent(contentId).let {
            experienceMapper.map(it)
        }
    }
}
