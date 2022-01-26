package com.appcues.data

import com.appcues.data.mapper.experience.ExperienceMapper
import com.appcues.data.remote.AppcuesRemoteSource
import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.DataGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DefaultDataGateway(
    private val appcuesRemoteSource: AppcuesRemoteSource,
    private val experienceMapper: ExperienceMapper = ExperienceMapper(),
) : DataGateway {

    override suspend fun getContent(contentId: String): Experience = withContext(Dispatchers.IO) {
        return@withContext experienceMapper.map(appcuesRemoteSource.getContent(contentId))
    }
}
