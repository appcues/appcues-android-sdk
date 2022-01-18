package com.appcues.data

import com.appcues.domain.entity.Experience
import com.appcues.domain.gateway.ExperienceGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class DefaultExperienceGateway : ExperienceGateway {

    override suspend fun getExperiences(contentId: String): List<Experience> = withContext(Dispatchers.IO) {
        return@withContext arrayListOf()
    }
}
